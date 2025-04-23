const router = require('express').Router();
const FeedTimestip = require('../../models/feedTimestip');

router.get('/:id', async (req, res) => {
	try {
		const feederId = req.params.id || null;

		console.log('get timestips');

		if (feederId == null) {
			return res.status(400).send();
		}

		// Save weight for graphs
		const feedTimestamps = await FeedTimestip.findAll({
			where: { feederId },
			order: [
				['hours', 'ASC'],
				['minutes', 'ASC'],
			],
		});

		console.log(feedTimestamps.length);

		//Hardware message
		let hardwareTimestamps = '';

		for (let i = 0; i < feedTimestamps.length; i++) {
			hardwareTimestamps += `${String(feedTimestamps[i].hours).padStart(2, '0')}:${String(feedTimestamps[i].minutes).padStart(2, '0')}`;

			if (i + 1 < feedTimestamps.length) {
				hardwareTimestamps += ',';
			}
		}

		if (hardwareTimestamps.length > 0) {
			console.log(hardwareTimestamps);
			res.status(200).send(hardwareTimestamps);
		} else {
			res.status(400).send();
		}
	} catch (error) {
		res.status(400).send();
	}
});

module.exports = router;
