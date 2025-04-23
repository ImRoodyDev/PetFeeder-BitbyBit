'use strict';
const FeederState = require('./feederState');
const { sequelize } = require('../../database');
const { Model, DataTypes } = require('sequelize');

class FeedTimestip extends Model {
	/**
	 * Helper method for defining associations.
	 * This method is not a part of Sequelize lifecycle.
	 * The `models/index` file will call this method automatically.
	 */
	static associate(models) {}
}

// Initialize Sequelize schema
FeedTimestip.init(
	{
		id: {
			type: DataTypes.UUID,
			primaryKey: true,
			allowNull: false,
			defaultValue: DataTypes.UUIDV4,
		},

		hours: {
			type: DataTypes.INTEGER,
			allowNull: true,
		},
		minutes: {
			type: DataTypes.INTEGER,
			allowNull: true,
		},

		feederId: {
			type: DataTypes.STRING,
			allowNull: true,
			references: {
				model: 'FeederStates',
				key: 'feederId',
			},
		},
	},
	{
		sequelize,
		modelName: 'FeedTimestip',
		tableName: 'FeedTimestips',
		// don't forget to enable timestamps!
		timestamps: true,
	}
);

module.exports = FeedTimestip;
