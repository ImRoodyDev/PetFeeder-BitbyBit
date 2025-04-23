const fs = require('fs');
const path = require('path');
const basename = path.basename(__filename);

const models = {};

// Loop throught the models folders and create field in db variable
fs.readdirSync(__dirname)
	.filter((fileName) => {
		return fileName.indexOf('.') !== 0 && fileName !== basename && fileName.slice(-3) === '.js' && fileName.indexOf('.test.js') === -1;
	})
	.forEach((file) => {
		// Model Path direction
		const modelPath = path.join(__dirname, file);

		// Import the model
		const model = require(modelPath); //(sequelize);
		console.log(model.name);
		models[model.name] = model;
	});

// Loop through all the keys in DB JSON to asociate them
Object.keys(models).forEach((modelName) => {
	if (models[modelName].associate) {
		models[modelName].associate(models);
	}
});

module.exports = models;
