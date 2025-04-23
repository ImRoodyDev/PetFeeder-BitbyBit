const router = require('express').Router();
const FeedTimestip = require('../../models/feedTimestip');
const { getSocket } = require('../../controllers/socketConnections');

router.get('/:id', async (req, res) => {
	try {
		const timestipId = req.params.id;

		if (timestipId == null) {
			return res.status(400).send({ message: 'Invalid request' });
		}

		const timestamps = await FeedTimestip.findByPk(timestipId);

		const deleted = await FeedTimestip.destroy({
			where: { id: timestipId },
			force: true,
		});

		// If user is already created / existing user
		if (deleted == 0) {
			return res.status(409).send({ message: 'Failed to delete timestamps' });
		}

		// Notify the specific user's WebSocket client
		const hardwareConnection = getSocket('hardware', timestamps.feederId);

		// Send the weight to the application connected via WebSocket
		if (hardwareConnection && hardwareConnection.readyState === 1) {
			hardwareConnection.send(`TIME:UPDATE`);
		}

		res.status(200).send({
			message: 'Deleted timestamps successfully',
		});
	} catch (error) {
		console.log(error);
		res.status(500).send({ message: 'Internal Server Error' });
	}
});

module.exports = router;
