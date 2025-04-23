const router = require('express').Router();
const FeederState = require('../../models/feederState');

router.get('/:id', async (req, res) => {
	try {
		const feederId = req.params.id || null;

		console.log('get porption size');

		if (feederId == null) {
			return res.status(400).send();
		}

		// Save weight for graphs
		const feederState = await FeederState.findOne({
			where: { feederId },
		});

		if (feederState == null) {
			return res.status(400).send('');
		}

		res.status(200).send(`${feederState.portionSize}`);
	} catch (error) {
		res.status(400).send('');
	}
});

module.exports = router;
