const { insertSocket, removeSocket, getSocket } = require('../../controllers/socketConnections');
const Feeder = require('../../models/feeder');
const url = require('url');

/** On connection handeling for websocket for notifing hardware on updates
 * @param {WebSocket} ws
 * @param {import('http').IncomingMessage} request
 */
const onConnection = async (ws, request) => {
	// Parse query parameters from the request URL
	const queryParams = new URLSearchParams(url.parse(request.url).query);
	const macAdress = queryParams.get('macId');
	const hardwareId = queryParams.get('hardwareId');

	console.log(macAdress, hardwareId);

	if (!macAdress || !hardwareId) {
		console.log('Missing query parameters');
		return ws.close(1008, 'Missing query parameters');
	}

	// Check if user is owner of the feeder
	const feeder = await Feeder.findOne({ where: { id: macAdress, hardwareId: hardwareId } });

	if (feeder == null) {
		console.log('Feeder not found');
		return ws.close(1008, 'Authorization rejected cause unknown feeder');
	}

	// Set feeder connection
	insertSocket(ws, 'hardware', macAdress, ws);

	console.log(`Hardware ${macAdress} connected to server\n`);

	// On close connection
	ws.on('close', () => {
		console.log(`Hardware ${macAdress} disconnected`);
		removeSocket('hardware', macAdress);
	});

	// Handle incoming messages from the hardware
	ws.on('message', (message) => {
		// console.log(`Message from ${macAdress}: ${message}`);

		if (String(message).includes('NEXTFEED:')) {
			return;
		}

		const messageArray = String(message).split(',');

		const weight = messageArray[0];
		const alarm = messageArray[1] || 'N/A';
		const ate = messageArray[2];

		// Notify the specific user's WebSocket client
		const applicationConnection = getSocket('application', macAdress);

		// Send the weight to the application connected via WebSocket
		if (applicationConnection && applicationConnection.readyState === 1) {
			applicationConnection.send(`WEIGHT:${weight}`);
			applicationConnection.send(`NEXTFEED-${alarm}`);
			applicationConnection.send(`ATE:${ate}`);
		}
	});
};

module.exports = onConnection;
