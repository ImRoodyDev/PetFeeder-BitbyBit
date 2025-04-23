const router = require('express').Router();
const Weight = require('../../models/weight');
const Sequelize = require('sequelize');
const { getSocket } = require('../../controllers/socketConnections');

router.post('/', async (req, res) => {
	try {
		const feederId = req.query.id || null;
		const weight = parseFloat(req.query.value);
		const portion = parseInt(req.query.portion);
		const timestamp = req.query.time;
		const hhmm = req.query.hhmm;

		console.log('update weight');
		console.log({ feederId, weight, portion, timestamp });

		if (!feederId) {
			return res.status(400).send({ message: 'Feeder ID is required.' });
		}

		// Save weight for graphs
		await Weight.create({
			feederId,
			amountEaten: (weight / portion) * 100,
			currentWeight: weight,
			localize_timestamps: timestamp,
			portionSize: portion,
			// hhmm: hhmm,
		});

		// Notify the specific user's WebSocket client
		const applicationConnection = getSocket('application', feederId);

		// Send the weight to the application connected via WebSocket
		if (applicationConnection && applicationConnection.readyState === 1) {
			applicationConnection.send('UPDATEWEIGHT');
		}

		// Send response
		res.status(200).send({ message: 'Weight data received successfully.' });
	} catch (error) {
		if (error instanceof Sequelize.ForeignKeyConstraintError) {
			console.log(error);
			return res.status(400).send({
				message: 'Invalid feeder ID. The device does not exist.',
			});
		} else {
			res.status(500).send({ message: 'An error occurred.' });
		}
	}
});

module.exports = router;
