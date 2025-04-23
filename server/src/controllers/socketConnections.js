const { get } = require('../routes/user/weeklyWeights');

/** Store WebSocket connections mapped to user IDs
 * @type {Map<string, WebSocket>}
 */
const applicationSockets = new Map();

/** Store WebSocket connections mapped to user IDs
 * @type {Map<string, WebSocket>}
 */
const hardwareSockets = new Map();

function insertSocket(socket, type, id) {
	if (type === 'application') {
		applicationSockets.set(id, socket);
	} else if (type === 'hardware') {
		hardwareSockets.set(id, socket);
	}
}

function removeSocket(type, id) {
	if (type === 'application') {
		applicationSockets.delete(id);
	} else if (type === 'hardware') {
		hardwareSockets.delete(id);
	}
}

function getSocket(type, id) {
	if (type === 'application') {
		return applicationSockets.get(id);
	} else if (type === 'hardware') {
		return hardwareSockets.get(id);
	}
}

module.exports = { insertSocket, removeSocket, getSocket };
