// Express Js
const express = require('express');
const applicationRoutes = express.Router();

// Middleware for routes
const { NoAuthentication, Authentication } = require('../middlewares/authentication');

// Routes handleing for express application
const Register = require('./user/register');
const Login = require('./user/login');
const Feeders = require('./user/feeders');
const UpdatePortion = require('./user/update-porpotion');
const LinkFeeder = require('./user/link-feeder');
const Verify = require('./user/verify');
const Daily = require('./user/dailyWeights');
const Weekly = require('./user/weeklyWeights');

const AllTimestamps = require('./user/timestips');
const UpdateTimestamp = require('./user/update-timestip');
const CreateTimeStamp = require('./user/create-timestip');
const DeleteTimestamps = require('./user/delete-timestip');

const GetTimeStips = require('./hardware/get-timestips');
const GetPorpotionSize = require('./hardware/get-porpotion');
const UpdateWeight = require('./hardware/get-timestips');
const onAppConnection = require('./sockets/onApplicationConnection');
const onHardwareConnection = require('./sockets/onHardwareConnection');

applicationRoutes.use('/login', NoAuthentication, Login);
applicationRoutes.use('/register', NoAuthentication, Register);
applicationRoutes.use('/link-device', Authentication, LinkFeeder);
applicationRoutes.use('/auth/verify', Authentication, Verify);

applicationRoutes.use('/feeders', Authentication, Feeders);
applicationRoutes.use('/feeder/update-portion', Authentication, UpdatePortion);
applicationRoutes.use('/feeder/timestamps', Authentication, AllTimestamps);
applicationRoutes.use('/feeder/update-timestamp', Authentication, UpdateTimestamp);
applicationRoutes.use('/feeder/create-timestamp', Authentication, CreateTimeStamp);
applicationRoutes.use('/feeder/delete-timestamp', Authentication, DeleteTimestamps);
applicationRoutes.use('/feeder/weights/weekly', Weekly);
applicationRoutes.use('/feeder/weights/daily', Daily);

applicationRoutes.use('/hardware/get-timestips', GetTimeStips);
applicationRoutes.use('/hardware/get-portionsize', GetPorpotionSize);
applicationRoutes.use('/hardware/update-weight', UpdateWeight);

/** On connection handeling for websocket
 * @param {WebSocket} ws
 * @param {import('http').IncomingMessage} request
 */
const webSocketOnConnection = (ws, request) => {
	// Get query parameters
	const queryParams = new URLSearchParams(request.url.split('?')[1]);
	const type = queryParams.get('type');

	console.log('\nSocket connection type:', type);

	// Check if type is valid
	if (type === 'application') {
		onAppConnection(ws, request);
	} else if (type == 'hardware') {
		onHardwareConnection(ws, request);
	} else {
		ws.close(1008, 'Invalid socket connection');
	}
};

module.exports = { applicationRoutes, webSocketOnConnection, webSocketOnConnection };
