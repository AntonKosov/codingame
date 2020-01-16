// https://www.codingame.com/training/easy/detective-pikaptcha-ep1

const codingame_detective_pikaptcha_ep1 = {
	run: (input, output) => {
		const height = input().split(' ')[1];

		const map = [];
		for (let i = 0; i < height; i++) {
			const line = input();
			map.push(line.split(''));
		}

		const adjacentCells = [
			[-1, 0],
			[0, -1],
			[1, 0],
			[0, 1]
		];

		const wall = "#";

		map.forEach((line, lineIndex) => {
			line.forEach((cell, columnIndex) => {
				if (cell !== wall) {
					let passages = 0;
					adjacentCells.forEach(adjacentCellOffset => {
						const adjacentCellLineIndex = lineIndex + adjacentCellOffset[0];
						const adjacentCellColumnIndex = columnIndex + adjacentCellOffset[1];
						const adjacentLine = map[adjacentCellLineIndex];
						if (adjacentLine) {
							const adjacentCell = adjacentLine[adjacentCellColumnIndex];
							if (adjacentCell && adjacentCell != wall) {
								passages++;
							}
						}
					});
					line[columnIndex] = passages.toString();
				}
			});

			// Write an action using console.log()
			// To debug: console.error('Debug messages...');
			output(line.join(''));
		});
	}
};

if (typeof readline !== 'undefined') {
	codingame_detective_pikaptcha_ep1.run(readline, console.log);
}

module.exports = codingame_detective_pikaptcha_ep1;