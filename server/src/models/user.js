'use strict';
const { sequelize } = require('../../database');
const { Model, DataTypes } = require('sequelize');
const bcrypt = require('bcrypt');

class User extends Model {
	/**
	 * Helper method for defining associations.
	 * This method is not a part of Sequelize lifecycle.
	 * The `models/index` file will call this method automatically.
	 */
	static associate(models) {}

	/* Compare User hash-password */
	static async compareHashPassword(password = '', hashPassword = '') {
		const validPassword = await bcrypt.compare(password, hashPassword);
		return validPassword;
	}
}

// Initialize Sequelize schema
User.init(
	{
		id: {
			type: DataTypes.UUID,
			primaryKey: true,
			allowNull: false,
			defaultValue: DataTypes.UUIDV4,
		},
		email: {
			type: DataTypes.STRING(255),
			allowNull: false,
			unique: true,
		},
		password: {
			type: DataTypes.STRING(255),
			allowNull: false,
			async set(value) {
				const HashKey = bcrypt.genSaltSync(Number(10));
				const hashPassword = bcrypt.hashSync(value, HashKey);

				// Hashing the value with an appropriate cryptographic hash function is better.
				this.setDataValue('password', hashPassword);
			},
		},
		ip: {
			type: DataTypes.STRING(255),
			allowNull: false,
		},
		countryCode: {
			type: DataTypes.CHAR(2),
			allowNull: true,
		},
		city: {
			type: DataTypes.STRING(255),
			allowNull: true,
		},
		country: {
			type: DataTypes.STRING(255),
			allowNull: true,
		},
		resetCount: {
			type: DataTypes.INTEGER,
			allowNull: false,
			defaultValue: 0,
		},
	},
	{
		sequelize,
		modelName: 'User',
		tableName: 'Users',
		// don't forget to enable timestamps!
		timestamps: true,
	}
);

module.exports = User;
