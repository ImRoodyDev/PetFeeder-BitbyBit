const express = require('express');
const { WebSocketServer } = require('ws');

const cors = require('cors');
const cookieParser = require('cookie-parser');
const { applicationRoutes, webSocketOnConnection } = require('./src/routes');

// Application port
const PORT = process.env.NODE_PORT || 3002;

// Configure cors
const corsConfig = cors({
	origin: '*',
	methods: 'GET,HEAD,PUT,PATCH,POST,OPTIONS',
	optionsSuccessStatus: 200,
});

// Initialize Express and WebSocket server
const app = express();
const wss = new WebSocketServer({ noServer: true });

// Initialize middlewares
// app.set('trust proxy', true);
app.use(corsConfig);
app.use(express.json());
app.use(cookieParser());

module.exports = () => {
	// Listener of server 1
	app.server = app.listen(PORT, () => {
		console.log(`Application Server listening on port ${PORT}`);
	});

	app.get('/', (req, res) => {
		res.send('Application Server is running.');
	});

	// Application server routes
	app.use('/api/', applicationRoutes);

	// WebSocket connection handling
	wss.on('connection', webSocketOnConnection);

	// Attach WebSocket server to the application server
	app.server.on('upgrade', (request, socket, head) => {
		wss.handleUpgrade(request, socket, head, (ws) => {
			wss.emit('connection', ws, request);
		});
	});
};
