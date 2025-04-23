const router = require('express').Router();
const Weight = require('../../models/weight');

router.get('/:id', async (req, res) => {
	try {
		console.log('GET /user/dailyWeights/:id');

		const monthNames = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];

		const feederId = req.params.id;
		const day = parseInt(req.query.day);
		const month = req.query.month;
		const yearNumber = parseInt(req.query.year);
		const monthNumber = monthNames.indexOf(month);

		if (monthNumber === -1 || isNaN(yearNumber)) {
			return res.status(400).send({ message: 'Invalid month or year' });
		}

		if (!feederId) {
			return res.status(400).send({ message: 'Invalid request' });
		}

		const Weights = await Weight.findAll({
			where: { feederId },
		});

		const daily = {};

		Weights.forEach((weight) => {
			const date = new Date(weight.createdAt);
			const recordMonth = date.getMonth();
			const recordYear = date.getFullYear();
			const recordDay = date.getDate();

			// Check if the weight is in the specified month and year
			if (recordMonth !== monthNumber || recordYear !== yearNumber || day !== recordDay) return;

			const key = `${recordDay}`; // Unique key for each day

			if (!daily[key]) {
				daily[key] = {
					id: key,
					day: key,
					totalPercentage: 0,
					count: 0,
					average: 0,
				};
			}

			// Update daily data
			daily[key].totalPercentage += (weight.currentWeight / weight.portionSize) * 100;
			daily[key].count += 1;
			daily[key].average = daily[key].totalPercentage / daily[key].count;
		});

		// Convert daily object to an array
		const dailyArray = Object.values(daily);

		res.status(200).send({
			message: 'Retrieved daily averages successfully',
			data: dailyArray,
		});
	} catch (error) {
		console.error(error); // Log the error for debugging
		res.status(500).send({ message: 'Internal Server Error' });
	}
});

module.exports = router;
