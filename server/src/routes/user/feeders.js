const router = require('express').Router();
const FeederState = require('../../models/feederState');
const FeedTimestip = require('../../models/feedTimestip');

router.get('/', async (req, res) => {
	try {
		const feeders = await FeederState.findAll({
			where: { userId: req.uuid },
			include: {
				model: FeedTimestip, // Include the associated model
			},
		});

		res.status(200).send({
			message: 'Retrieved feeders successfully',
			data: feeders?.map((feederState) => {
				return {
					feederId: feederState.feederId,
					portionSize: feederState.portionSize,
					timestamps: feederState.FeedTimestips.map((timestamp) => {
						return {
							id: timestamp.id,
							time: `${String(timestamp.hours).padStart(2, '0')}:${String(timestamp.minutes).padStart(2, '0')}`,
						};
					}),
				};
			}),
		});
	} catch (error) {
		res.status(500).send({ message: 'Internal Server Error' });
	}
});

module.exports = router;
