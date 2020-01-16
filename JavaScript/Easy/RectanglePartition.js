(() => {
	const [width, height, countX, countY] = readline().split(' ').map(v => parseInt(v));
	const partitionsX = [0, ...readline().split(' ').map(v => parseInt(v)), width];
	const partitionsY = [0, ...readline().split(' ').map(v => parseInt(v)), height];

	let squares = 0;

	for (let leftXIndex = 0; leftXIndex < partitionsX.length - 1; leftXIndex++) {
		for (let rightXIndex = leftXIndex + 1; rightXIndex < partitionsX.length; rightXIndex++) {
			let sizeX = partitionsX[rightXIndex] - partitionsX[leftXIndex];
			for (let upYIndex = 0; upYIndex < partitionsY.length - 1; upYIndex++) {
				for (let downYIndex = upYIndex + 1; downYIndex < partitionsY.length; downYIndex++) {
					let sizeY = partitionsY[downYIndex] - partitionsY[upYIndex];
					if (sizeX === sizeY) {
						squares++;
					}
				}
			}
		}
	}

	// Write an action using console.log()
	// To debug: console.error('Debug messages...');
	console.log(squares);
})();