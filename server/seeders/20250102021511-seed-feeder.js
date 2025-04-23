'use strict';

module.exports = {
	up: async (queryInterface, Sequelize) => {
		await queryInterface.bulkInsert('Feeders', [
			{
				id: 'c3d2aefc2745',
				hardwareId: '5bb95ebb-1b78-440c-b652-7d38079b22f8',
				manufactered: new Date('2022-01-01T00:00:00Z'),
				createdAt: new Date(),
				updatedAt: new Date(),
			},
			{
				id: 'a1b2c3d4e5f6',
				hardwareId: '71eaf123-6f54-4f8c-9d5a-2b3e487c9e47',
				manufactered: new Date('2022-01-01T00:00:00Z'),
				createdAt: new Date(),
				updatedAt: new Date(),
			},
			{
				id: 'f1e2d3c4b5a6',
				hardwareId: '4a2b567d-8f32-4d5a-9e76-3b2c4f1e5d6a',
				manufactered: new Date('2022-06-15T00:00:00Z'),
				createdAt: new Date(),
				updatedAt: new Date(),
			},
			{
				id: 'b1c2d3e4f5a6',
				hardwareId: '123abc45-67de-89f0-ab12-3456c7d89ef0',
				manufactered: new Date('2023-01-10T00:00:00Z'),
				createdAt: new Date(),
				updatedAt: new Date(),
			},
		]);
	},

	down: async (queryInterface, Sequelize) => {
		await queryInterface.bulkDelete('Feeders', null, {});
	},
};
