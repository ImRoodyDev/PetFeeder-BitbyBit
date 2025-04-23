const router = require('express').Router();
const FeedTimestip = require('../../models/feedTimestip');

router.get('/:id', async (req, res) => {
	try {
		const feederId = req.params.id;

		if (feederId == null) {
			return res.status(400).send({ message: 'Invalid request' });
		}

		const [createdTimestip, created] = await FeedTimestip.findOrCreate({
			where: { feederId, hours: null, minutes: null },
			defaults: {
				feederId,
				hours: null,
				minutes: null,
			},
		});

		// If user is already created / existing user
		if (!created) {
			return res.status(409).send({ message: 'Already got an empty timestamp' });
		}

		res.status(200).send({
			message: 'Created timestamps successfully',
			id: createdTimestip.id,
		});
	} catch (error) {
		res.status(500).send({ message: 'Internal Server Error' });
	}
});

module.exports = router;
