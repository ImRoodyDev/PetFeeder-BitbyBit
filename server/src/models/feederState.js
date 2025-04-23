'use strict';
const User = require('./user');
const Feeder = require('./feeder');
const { sequelize } = require('../../database');
const { Model, DataTypes } = require('sequelize');
const FeedTimestip = require('./feedTimestip');

class FeederState extends Model {
	/**
	 * Helper method for defining associations.
	 * This method is not a part of Sequelize lifecycle.
	 * The `models/index` file will call this method automatically.
	 */
	static associate(models) {
		this.belongsTo(User, { foreignKey: 'userId' }); // , targetKey: 'code'
		this.belongsTo(Feeder, { foreignKey: 'feederId' }); // , targetKey: 'code'
		this.hasMany(FeedTimestip, { foreignKey: 'feederId' }); // , targetKey: 'code'
	}
}

// Initialize Sequelize schema
FeederState.init(
	{
		feederId: {
			type: DataTypes.STRING,
			primaryKey: true,
			allowNull: false,
			references: {
				model: 'Feeders',
				key: 'id',
			},
		},
		userId: {
			type: DataTypes.UUID,
			allowNull: true,
			references: {
				model: 'Users',
				key: 'id',
			},
		},

		storageWeight: {
			type: DataTypes.INTEGER,
			allowNull: false,
			defaultValue: 0,
		},

		portionSize: {
			type: DataTypes.INTEGER,
			allowNull: false,
			defaultValue: 0,
		},
	},
	{
		sequelize,
		modelName: 'FeederState',
		tableName: 'FeederStates',
		// don't forget to enable timestamps!
		timestamps: true,
	}
);

module.exports = FeederState;
