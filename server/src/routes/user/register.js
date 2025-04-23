const router = require('express').Router();
const User = require('../../models/user');
const { generateAccessToken, generateRefreshToken, saveRefreshToken } = require('../../controllers/authentication');
const { validateRegister } = require('../../utils/validationUtils');
const { getLocationTime } = require('../../utils/dateUtils');

router.post('/', async (req, res) => {
	try {
		console.log('registering user');
		// console.log('Headers:', req.headers);
		console.log('Body:', req.body);

		// Retrieve the IP address from the request object
		let clientIp = req.headers['x-client-ip'] || req.socket.remoteAddress || null;

		// If it's an IPv6-mapped IPv4 address, extract the IPv4 address.
		if (clientIp && clientIp.startsWith('::ffff:')) {
			clientIp = clientIp.substring(7); // Remove the ::ffff: prefix.
		}

		// Device geolocation and time information
		const deviceGeolocation = await getLocationTime(clientIp);

		// Validate request body
		const [registerError, register] = validateRegister(req.body);

		// Check if postBody is safe
		if (registerError) {
			return res.status(400).send({ message: registerError.message.replace(/'/g, '') });
		}

		// Find or create User , automatically it will hash the passwoord before storing it on the database
		const [user, created] = await User.findOrCreate({
			where: { email: register.email },
			defaults: {
				...register,
				ip: clientIp,
				countryCode: deviceGeolocation.countryCode,
				country: deviceGeolocation.country,
				city: deviceGeolocation.city,
			},
		});

		// If user is already created / existing user
		if (!created) {
			return res.status(409).send({ message: 'User with given email already Exist!' });
		}

		// Handle authentication tokens
		const refreshToken = generateRefreshToken(user.id);
		const accessToken = generateAccessToken(user.id);

		// Save and send request
		saveRefreshToken(res, refreshToken);

		res.status(200).send({
			accessToken,
			message: 'User created successfully',
		});
	} catch (error) {
		console.error('Error in user registration:', error);
		res.status(500).send({ message: 'Internal Server Error' });
	}
});

module.exports = router;
