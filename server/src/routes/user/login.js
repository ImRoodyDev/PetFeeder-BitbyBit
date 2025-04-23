const router = require('express').Router();
const User = require('../../models/user');
const FeederState = require('../../models/feederState');
const { generateAccessToken, generateRefreshToken, saveRefreshToken } = require('../../controllers/authentication');
const { validateLogin } = require('../../utils/validationUtils');

router.post('/', async (req, res) => {
	try {
		console.log('logging user');

		// Validate request body
		const [loginError, login] = validateLogin(req.body);

		// Check if loginBody is safe
		if (loginError) {
			return res.status(400).send({ message: loginError.message.replace(/'/g, '') });
		}

		// Find user in the database
		const user = await User.findOne({ where: { email: login.email } });
		const ownedFeederCount = user ? await FeederState.count({ where: { userId: user.id } }) : 0;

		// Check wheather user exsist
		if (!user) {
			return res.status(401).send({ message: 'Invalid credentials' });
		}

		// Combined your password with the hash password
		const validPassword = await User.compareHashPassword(login.password, user.password);

		// Check if the password is valid
		if (!validPassword) {
			return res.status(401).send({ message: 'Invalid Password' });
		}

		// Handle authentication tokens
		const refreshToken = generateRefreshToken(user.id);
		const accessToken = generateAccessToken(user.id);

		// Save refresh token
		saveRefreshToken(res, refreshToken);

		// Send response to client
		res.status(200).send({
			accessToken,
			message: 'User successfully logged in',
			ownedFeeders: ownedFeederCount,
		});
	} catch (error) {
		console.error('Error in user login:', error);
		res.status(500).send({ message: 'Internal Server Error' });
	}
});

module.exports = router;
