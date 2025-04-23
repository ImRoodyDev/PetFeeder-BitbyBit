const router = require('express').Router();
const FeedTimestip = require('../../models/feedTimestip');

router.get('/:feederId', async (req, res) => {
	try {
		const feederId = req.params.feederId || null;

		console.log('timestips of :', feederId);

		if (feederId == null) {
			return res.status(400).send({ message: 'Invalid request' });
		}

		const timestamps =
			(await FeedTimestip.findAll({
				where: { feederId },
				order: [
					['hours', 'ASC'],
					['minutes', 'ASC'],
				],
			})) || [];

		res.status(200).send({
			message: 'Timestamps fetched successfully',
			data: timestamps?.map((timestamp) => {
				return {
					id: timestamp.id,
					time: `${String(timestamp.hours).padStart(2, '0')}:${String(timestamp.minutes).padStart(2, '0')}`,
				};
			}),
		});
	} catch (error) {
		res.status(500).send({ message: 'Internal Server Error' });
	}
});

module.exports = router;
