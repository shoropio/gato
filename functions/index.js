const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

/**
 * Cloud Function: when a match document is created in Firestore,
 * send a push notification to the opponent via FCM.
 */
exports.sendMatchNotification = functions.firestore
  .document('matches/{matchId}')
  .onCreate(async (snap, context) => {
    const match = snap.data();
    const opponentUid = match.playerO;
    const challengerName = match.playerXName || 'Alguien';
    const matchId = context.params.matchId;

    try {
      // Get opponent's FCM token
      const userDoc = await admin.firestore()
        .collection('users')
        .doc(opponentUid)
        .get();

      const fcmToken = userDoc.data()?.fcmToken;
      if (!fcmToken) {
        console.log(`No FCM token for user ${opponentUid}`);
        return;
      }

      const message = {
        token: fcmToken,
        notification: {
          title: '¡Nuevo desafío PvP!',
          body: `${challengerName} te ha retado a una partida`
        },
        data: {
          matchId: matchId,
          challengerName: challengerName
        },
        android: {
          priority: 'high'
        }
      };

      await admin.messaging().send(message);
      console.log(`Notification sent to ${opponentUid} for match ${matchId}`);
    } catch (error) {
      console.error('Error sending notification:', error);
    }
  });
