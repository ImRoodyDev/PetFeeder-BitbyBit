'use strict';
const jwt = require('jsonwebtoken');
const User = require('../models/user');

// Enviroment
const ENV = process.env.NODE_ENV || 'development';
// Public key
const REFRESH_PUBLICKEY = process.env.REFRESH_PUBLICKEY;
const ACCESS_PUBLICKEY = process.env.REFRESH_PUBLICKEY;
const RESET_PUBLICKEY = process.env.REFRESH_PUBLICKEY;
// Private key
const REFRESH_PRIVATEKEY = process.env.REFRESH_PRIVATEKEY;
const ACCESS_PRIVATEKEY = process.env.REFRESH_PRIVATEKEY;
const RESET_PRIVATEKEY = process.env.RESET_PRIVATEKEY;
// Token expiry
const ACCESS_TOKEN_EXPIRY = process.env.ACCESS_TOKEN_EXPIRY || '10h';

/** Generate reset password token */
const generateResetToken = async function (uuid = '', resetCount = 0) {
	const token = jwt.sign({ uuid, resetCount }, RESET_PRIVATEKEY, {
		algorithm: 'RS256',
	});
	return token;
};
/** Generate refresh token (long-lived)
 * A refresh token allows the user to get a new access token without needing to log in again.
 *
 * A refresh token is long-lived and used to get a new access token when the current one expires.
 *
 */
const generateRefreshToken = function (uuid = '', resetCount = 0, role = 'user') {
	const token = jwt.sign({ uuid, resetCount, role }, REFRESH_PRIVATEKEY, {
		algorithm: 'RS256',
	});
	return token;
};
/** Generate access token (short-lived)
 * When the access token expires, the client (e.g., a front-end app)
 * requests a new access token using the refresh token.
 * An access token is short-lived and used to access protected resources.

 * The server verifies the refresh token, and if valid, issues a new access token.
 */
const generateAccessToken = function (uuid = '') {
	const token = jwt.sign({ uuid }, ACCESS_PRIVATEKEY, {
		algorithm: 'RS256',
		expiresIn: ACCESS_TOKEN_EXPIRY,
	});
	return token;
};
/** Verify token */
const verifyToken = (token, publicKey, options) => {
	return new Promise((resolve, reject) =>
		jwt.verify(token, publicKey, options, (err, decoded) => {
			if (err) return reject(err);
			resolve(decoded);
		})
	);
};

/** Verify access & refresh token
 
 *  Return :
 *  @param Boolean validToken
 *  @param User userModel  
 */
const verifyTokens = async function (refresh = '', access = '') {
	// User Id
	let userId = null;
	// User role
	let role = null;
	// Refresh token
	var refreshTokenValid = false;
	// Access token
	var accessTokenValid = false;
	// New access token
	let updatedAccesstoken = null;

	// Verify refresh token
	try {
		const refreshDecoded = await verifyToken(refresh, REFRESH_PUBLICKEY, {
			algorithms: ['RS256'],
		});

		userId = refreshDecoded.uuid;
		role = refreshDecoded.role;
		refreshTokenValid = true;
	} catch (error) {
		refreshTokenValid = false;
	}

	// If refresh token is valid
	if (refreshTokenValid) {
		// Find user by id in database
		const user = await User.findByPk(userId);

		// Check wheather user exsist
		if (!user) {
			refreshTokenValid = false;
		}

		// Verify access token
		try {
			const accessDecoded = await verifyToken(access, ACCESS_PUBLICKEY, {
				algorithms: ['RS256'],
			});
			accessTokenValid = true;
		} catch (error) {
			if (error.name === 'TokenExpiredError') {
				updatedAccesstoken = generateAccessToken(userId);
			} else {
				accessTokenValid = false;
			}
		}
	}

	// Validations passed
	const valid = refreshTokenValid && accessTokenValid;

	return { valid, userId, role, updatedAccesstoken };
};
/** Verify Reset token */
const verifyResetToken = async function (reset = '') {
	// Verify token
	try {
		const decoded = await verifyToken(reset, RESET_PUBLICKEY, {
			algorithms: ['RS256'],
		});

		const user = await User.findByPk(decoded.uuid, {
			attributes: ['id', 'resetCount'],
		});

		return user && user.resetCount === decoded.resetCount ? [true, user] : [false];
	} catch (error) {
		return [false];
	}
};
/** Save tokens */
const saveRefreshToken = function (response, refresh = '') {
	response.cookie('refreshToken', refresh, {
		httpOnly: ENV === 'production' ? true : false,
		secure: ENV === 'production',
		sameSite: 'Strict',
	});
};
/** Delete tokens */
const clearTokens = function (response) {
	response.clearCookie('refreshToken');
};

module.exports = {
	generateRefreshToken,
	generateAccessToken,
	generateResetToken,
	verifyTokens,
	verifyResetToken,
	saveRefreshToken,
	clearTokens,
};
