const { verifyTokens, clearTokens } = require('../controllers/authentication');

/** Check if user authentication is valid and active*/
const Authentication = async (req, res, next) => {
	try {
		// Access token
		const accessToken = req.headers.authorization?.split(' ')[1];

		// Refresh token
		const refreshToken = req.cookies.refreshToken;

		// Check tokens
		if (typeof accessToken != 'string' || typeof refreshToken != 'string') {
			throw new Error('Authentication failed');
		}

		// Tokens availability
		if (refreshToken == null || accessToken == null) {
			throw new Error('User not logged in');
		}

		// Is user Authentication token valid ?
		const { valid, userId, updatedAccesstoken } = await verifyTokens(refreshToken, accessToken);

		// Set User Id variable
		req.uuid = userId;

		// Attach the new access token to cookie
		if (valid && updatedAccesstoken) {
			res.setHeader('x-authentication', updatedAccesstoken);
		}

		// When token is valid
		if (valid) {
			next(); // Can Access Path
		} else {
			// Clear saved tokens
			clearTokens();

			return res.status(400).send({ message: 'Authentication failed' });
		}
	} catch (error) {
		console.log(error.message);
		return res.status(500).send({ message: 'Authentication failed' });
	}
};

/** Check if user is not already signed in  */
const NoAuthentication = async (req, res, next) => {
	try {
		// Access token
		const accessToken = req.headers.authorization?.split(' ')[1];

		// Refresh token
		const refreshToken = req.cookies.refreshToken;

		// Is user Authentication token valid ?
		const { valid } = await verifyTokens(refreshToken, accessToken);

		// When token is valid
		if (valid) {
			return res.status(400).send({ message: 'User already signed in' });
		} else {
			next();
		}
	} catch (error) {
		return res.status(500).send({ message: error.message });
	}
};

module.exports = { NoAuthentication, Authentication };
