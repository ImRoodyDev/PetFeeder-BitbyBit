const router = require('express').Router();
const Weight = require('../../models/weight');

router.get('/:id', async (req, res) => {
	try {
		console.log('GET ' + req.originalUrl);
		const monthNames = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];

		const feederId = req.params.id;
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

		const weekly = {};

		Weights.forEach((weight) => {
			const date = new Date(weight.createdAt);
			const recordMonth = date.getMonth();
			const recordYear = date.getFullYear();

			// Check if the weight is in the specified month and year
			if (recordMonth !== monthNumber || recordYear !== yearNumber) return;

			const week = Math.ceil(date.getDate() / 7); // Calculate the week of the month (1-5)
			const key = `Week ${week}`;

			if (!weekly[key]) {
				weekly[key] = {
					id: key,
					week: key,
					totalPercentage: 0,
					count: 0,
					average: 0,
				};
			}

			// Update weekly data
			weekly[key].totalPercentage += (weight.currentWeight / weight.portionSize) * 100;
			weekly[key].count += 1;
			weekly[key].average = weekly[key].totalPercentage / weekly[key].count;
		});

		// Convert weekly object to an array
		const weeklyArray = Object.values(weekly);
		console.log(weeklyArray.length);
		res.status(200).send({
			message: 'Retrieved weekly averages successfully',
			data: weeklyArray,
		});
	} catch (error) {
		console.error(error); // Log the error for debugging
		res.status(500).send({ message: 'Internal Server Error' });
	}
});

module.exports = router;
