const router = require('express').Router();
const FeedTimestip = require('../../models/feedTimestip');
const { getSocket } = require('../../controllers/socketConnections');

router.post('/:feederId', async (req, res) => {
	try {
		const feederId = req.params.feederId;
		const timestampId = req.body.timestampId;
		const time = String(req.body.time);

		console.log(feederId, timestampId, time);

		if (feederId == null) {
			return res.status(400).send({ message: 'Invalid request' });
		}

		const updated = await FeedTimestip.update(
			{ hours: Number(time.split(':')[0]), minutes: Number(time.split(':')[1]) },
			{
				where: { id: timestampId, feederId },
			}
		);

		// If user is already created / existing user
		if (!updated) {
			return res.status(409).send({ message: 'Failed to update timestamps' });
		}

		// Notify the specific user's WebSocket client
		const hardwareConnection = getSocket('hardware', feederId);

		// Send the weight to the application connected via WebSocket
		if (hardwareConnection && hardwareConnection.readyState === 1) {
			hardwareConnection.send(`TIME:UPDATE`);
		}

		res.status(200).send({
			message: 'Updated timestamps successfully',
		});
	} catch (error) {
		console.log(error);
		res.status(500).send({ message: 'Internal Server Error' });
	}
});

module.exports = router;
