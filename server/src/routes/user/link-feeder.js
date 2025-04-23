const router = require('express').Router();
const FeederState = require('../../models/feederState');
const Sequelize = require('sequelize');

router.post('/', async (req, res) => {
	try {
		const feederId = req.body.id || '';

		console.log('link feeder to user ' + feederId);

		if (feederId == null) {
			return res.status(400).send({ message: 'Invalid request' });
		}

		const [feederState, created] = await FeederState.findOrCreate({
			where: { feederId },
			defaults: {
				feederId: feederId,
				userId: req.uuid,
			},
		});

		// If user is already created / existing user
		if (!created) {
			return res.status(409).send({ message: 'Pet feeder is already linked to an account' });
		}

		res.status(200).send({
			message: 'Linked feeder successfully',
		});
	} catch (error) {
		if (error instanceof Sequelize.ForeignKeyConstraintError) {
			res.status(400).send({
				message: 'This feeder is not registered in the system or is already linked to another account',
			});
		} else if (error instanceof Sequelize.DatabaseError) {
			return res.status(400).send({
				message: 'This feeder is not registered in the system or is already linked to another account',
			});
		} else {
			res.status(500).send({ message: 'Internal Server Error' });
		}
	}
});

module.exports = router;
