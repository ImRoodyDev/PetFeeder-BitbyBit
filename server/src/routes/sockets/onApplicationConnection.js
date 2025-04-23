const { insertSocket, removeSocket, getSocket } = require('../../controllers/socketConnections');
const FeederState = require('../../models/feederState');
const { verifyTokens } = require('../../controllers/authentication');
const url = require('url');

/** On connection handeling for websocket
 * @param {WebSocket} ws
 * @param {import('http').IncomingMessage} request
 */
const onConnection = async (ws, request) => {
	// Parse query parameters from the request URL
	const queryParams = new URLSearchParams(url.parse(request.url).query);
	const feederId = queryParams.get('feederId');
	const refreshToken = queryParams.get('refreshToken');
	const accessToken = request.headers['authorization'].split(' ')[1];

	// Verify tokens
	const { valid, userId } = await verifyTokens(refreshToken, accessToken);
	if (!valid) {
		ws.close(1008, 'Authorization rejected'); // Close the connection if userId is missing
		return;
	}

	// Check if user is owner of the feeder
	const feeder = await FeederState.findOne({ where: { feederId, userId } });
	if (!feeder) {
		return ws.close(1008, 'Authorization rejected');
	}

	// Set feeder connection
	insertSocket(ws, 'application', feederId);

	console.log(`Application User ${userId} connected of feeder ${feederId}`);

	ws.send('Connection established');

	// On close connection
	ws.on('close', () => {
		console.log(`User ${userId} disconnected`);
		removeSocket('application', feederId);
	});

	// Handle incoming messages from the Sofware
	ws.on('message', (message) => {
		console.log(`Message from ${feederId}: ${String(message)}`);

		// Notify the specific user's WebSocket client
		const hardwareConnection = getSocket('hardware', feederId);

		// Send the weight to the application connected via WebSocket
		if (hardwareConnection && hardwareConnection.readyState === 1) {
			hardwareConnection.send(String(message));
		}
	});
};

module.exports = onConnection;
