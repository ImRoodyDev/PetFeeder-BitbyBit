'use strict';
const { sequelize } = require('../../database');
const { Model, DataTypes } = require('sequelize');

class FeederState extends Model {
	/**
	 * Helper method for defining associations.
	 * This method is not a part of Sequelize lifecycle.
	 * The `models/index` file will call this method automatically.
	 */
	static associate(models) {}
}

// Initialize Sequelize schema
FeederState.init(
	{
		id: {
			type: DataTypes.STRING,
			primaryKey: true,
			allowNull: false,
		},

		hardwareId: {
			type: DataTypes.STRING,
			unique: true,
			allowNull: false,
		},

		manufactered: {
			type: DataTypes.DATE,
			allowNull: false,
		},
	},
	{
		sequelize,
		modelName: 'Feeder',
		tableName: 'Feeders',
		// don't forget to enable timestamps!
		timestamps: true,
	}
);

module.exports = FeederState;
