const router = require('express').Router();

router.get('', async (req, res) => {
	try {
		res.status(200).send({
			message: 'Tokens verified successfully',
		});
	} catch (error) {
		res.status(500).send({ message: 'Internal Server Error' });
	}
});

module.exports = router;
