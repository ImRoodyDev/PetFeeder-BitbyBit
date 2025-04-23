const router = require('express').Router();
const FeederState = require('../../models/feederState');
const { getSocket } = require('../../controllers/socketConnections');

router.post('/:id', async (req, res) => {
	try {
		const feederId = req.params.id;
		const weight = Number(req.body.weight) || null;

		if (feederId == null) {
			return res.status(400).send({ message: 'Invalid request' });
		}

		const feederState = await FeederState.findOne({
			where: { feederId, userId: req.uuid },
		});

		feederState.portionSize = weight;
		await feederState.save();

		// If user is already created / existing user
		if (!feederState) {
			return res.status(409).send({ message: 'Feeder is not linked to user' });
		}

		// Notify the specific user's WebSocket client
		const hardwareConnection = getSocket('hardware', feederId);

		// Send the weight to the application connected via WebSocket
		if (hardwareConnection && hardwareConnection.readyState === 1) {
			hardwareConnection.send(`PORTION:${weight}`);
		}

		res.status(200).send({
			message: 'Updated weight successfully',
		});
	} catch (error) {
		console.log(error);
		res.status(500).send({ message: 'Internal Server Error' });
	}
});

module.exports = router;
