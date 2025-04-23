const { Sequelize } = require('sequelize');
const config = require('./config/config.json');
const fs = require('fs');
const path = require('path');

const env = process.env.NODE_ENV || 'development';
const { database, username, password, host, dialect, port } = config[env];

// Create a new Sequelize instance
const sequelize = new Sequelize(database, username, password, { host, dialect, port, logging: false });

/** Initialize the Sequelize connection
 * @returns {Promise<void>}
 */
async function initializeSequelize() {
	try {
		// Authenticate the connection
		await sequelize.authenticate();
		console.log('Database connection established successfully.');

		// Initialize models assotions
		const models = require('./src/models');

		const reload = false;

		// Synchronize models with the database (create tables if not exist)
		await sequelize.sync({
			force: reload, //false   Set `force: true` to drop and recreate tables
			alter: reload, // flase This checks what is the current state of the table in the database (which columns it has, what are their data types, etc)
		});

		console.log('Tables are synchronized.');

		if (env === 'development' && reload) {
			// Run seeders to populate the database with initial data
			await runSeeders();
		}
	} catch (error) {
		// Improved error logging with more detail
		console.error('Unable to connect to the database:', error.message);
		//console.log(error);
	}
}

/** Run seeders to populate the database with initial data
 * @returns {Promise<void>}
 */
async function runSeeders() {
	// folder of seed is ./seeders/
	const seedersDir = path.join(__dirname, 'seeders');

	try {
		// Get all seed files sorted by name
		const seedFiles = fs
			.readdirSync(seedersDir)
			.filter((file) => file.endsWith('.js'))
			.sort(); // Sort for sequential execution

		// Execute each seed file
		for (const file of seedFiles) {
			const filePath = path.join(seedersDir, file);
			const seeder = require(filePath);

			// Execute the seed's up function with queryInterface and Sequelize
			await seeder.up(sequelize.getQueryInterface(), Sequelize);
			console.log(`✅ Successfully executed seeder: ${file}`);
		}
	} catch (error) {
		console.error('❌ Seeder error:', error.message);
		// throw error; // Propagate error to initializeSequelize
	}
}

/** Disconnect the Sequelize connection gracefully
 * @returns {Promise<void>}
 */
async function disconnectSequelize() {
	try {
		await sequelize.close();
		console.log('Database connection closed successfully.');
	} catch (error) {
		console.error('Error while closing the database connection:', error.message);
	}
}

// Function to initialize and authenticate the database connection
module.exports = { sequelize, initializeSequelize, disconnectSequelize };
