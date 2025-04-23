'use strict';
const Feeder = require('./feeder');
const { sequelize } = require('../../database');
const { Model, DataTypes } = require('sequelize');

class Weight extends Model {
	/**
	 * Helper method for defining associations.
	 * This method is not a part of Sequelize lifecycle.
	 * The `models/index` file will call this method automatically.
	 */
	static associate(models) {
		this.belongsTo(Feeder, { foreignKey: 'feederId' }); // , targetKey: 'code'
	}
}

// Initialize Sequelize schema
Weight.init(
	{
		feederId: {
			type: DataTypes.STRING,
			allowNull: true,
			references: {
				model: 'Feeders',
				key: 'id',
			},
		},

		currentWeight: {
			type: DataTypes.FLOAT,
			allowNull: false,
			defaultValue: 0,
		},

		portionSize: {
			type: DataTypes.INTEGER,
			allowNull: false,
			defaultValue: 0,
		},

		localize_timestamps: {
			type: DataTypes.TIME,
			allowNull: false,
		},
	},
	{
		sequelize,
		modelName: 'Weight',
		tableName: 'Weights',
		// don't forget to enable timestamps!
		timestamps: true,
	}
);

module.exports = Weight;
