const feederId = 'c3d2aefc2745';
const portionSize = 1000; // grams

module.exports = {
	up: async (queryInterface, Sequelize) => {
		const weights = [];
		const now = new Date();

		// Generate one month of data (assuming 30 days)
		for (let day = 0; day < 30; day++) {
			for (let update = 0; update < 4; update++) {
				const currentDate = new Date();
				currentDate.setDate(now.getDate() - day);
				currentDate.setHours(6 * update, 0, 0, 0); // 6-hour intervals

				weights.push({
					feederId,
					currentWeight: Math.random() * 1000, // Random weight between 0 and 1000 grams
					portionSize,
					localize_timestamps: currentDate,
					createdAt: currentDate,
					updatedAt: currentDate,
				});
			}
		}

		// Bulk insert into the Weights table
		await queryInterface.bulkInsert('Weights', weights);
	},

	down: async (queryInterface, Sequelize) => {
		await queryInterface.bulkDelete('Weights', null, {});
	},
};

/*
module.exports = {
	up: async (queryInterface, Sequelize) => {
		const weights = [];
		const now = new Date();
		now.setHours(0, 0, 0, 0); // Normalize to midnight

		// Generate 4 months of data (120 days)
		for (let day = 0; day < 120; day++) {
			for (let update = 0; update < 4; update++) {
				const currentDate = new Date(now);
				// Subtract days from original date
				currentDate.setDate(now.getDate() + day);
				// Set to 6-hour intervals (00:00, 06:00, 12:00, 18:00)
				currentDate.setHours(6 * update, 0, 0, 0);

				weights.push({
					feederId,
					currentWeight: Math.random() * 1000,
					portionSize,
					localize_timestamps: currentDate,
					createdAt: currentDate,
					updatedAt: currentDate,
				});
			}
		}

		await queryInterface.bulkInsert('Weights', weights);
	},

	down: async (queryInterface, Sequelize) => {
		await queryInterface.bulkDelete('Weights', null, {});
	},
};
*/
