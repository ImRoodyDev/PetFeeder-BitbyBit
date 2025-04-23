const Joi = require('joi');
const passwordComplexity = require('joi-password-complexity');

/** Password specifications */
const complexityOptions = {
	min: 7,
	max: 26,
	lowerCase: 1,
	upperCase: 1,
	numeric: 2,
	symbol: 0,
	requirementCount: 3, // At least 3 of the above rules must be satisfied
};

function validateLogin(data) {
	const validator = Joi.object({
		email: Joi.string().email().trim().required().label('Email'),
		password: Joi.string().max(26),
	});

	const { error, value } = validator.validate(data);
	return [error, value];
}

/** Validate post body for registering account */
function validateRegister(postBody) {
	const validator = Joi.object({
		email: Joi.string().email().lowercase().trim().required().label('Email').messages({
			'string.empty': `Your email address cannot be empty. Please enter a valid email.`,
			'string.email': `The email address entered is invalid. Please use a valid format like example@domain.com.`,
			'any.required': `The Email field is required. Please provide your email address.`,
		}),

		password: passwordComplexity(complexityOptions).trim().required().label('Password').messages({
			'string.empty': `Your password cannot be empty. Please enter a valid password.`,
			'string.length': `Your password does not meet the required complexity. Please follow the specified guidelines.`,
			'string.pattern': `Your password does not meet the required complexity. Please follow the specified guidelines.`,
			'any.required': `The Password field is required. Please provide a password.`,
		}),
	});

	const { error, value } = validator.validate(postBody);
	return [error, value];
}

function validateEmail(data) {
	const validator = Joi.object({
		email: Joi.string().email().lowercase().trim().required().label('Email'),
	});

	const { error, value } = validator.validate(data);

	return [error, value];
}

function validatePassword(data) {
	const validator = Joi.object({
		password: passwordComplexity(complexityOptions).trim().required().label('Password'),
	});

	const { error, value } = validator.validate(data);

	return [error, value];
}

module.exports = {
	validateLogin,
	validateRegister,
	validateEmail,
	validatePassword,
};
