// https://www.codingame.com/ide/puzzle/1d-spreadsheet

(() => {
	class Cell {
		constructor(operation, first, second) {
			this.operation = operation;
			this.first = first;
			this.second = second;
			this.value = undefined;
		}
	}

	let getValue = cell => {
		if (cell.value === undefined) {
			cell.value = operations[cell.operation](cell);
		}

		return cell.value;
	};

	let argValue = arg => {
		let value = 0;

		if (arg[0] === '$') {
			let cellIndex = arg.substring(1);
			value = parseInt(getValue(data[cellIndex]));
		} else {
			value = parseInt(arg);
		}

		return value;
	};

	let operations = {
		"VALUE": cell => argValue(cell.first),
		"ADD": cell => argValue(cell.first) + argValue(cell.second),
		"SUB": cell => argValue(cell.first) - argValue(cell.second),
		"MULT": cell => argValue(cell.first) * argValue(cell.second)
	};

	let data = [];
	const numberOfCells = parseInt(readline());
	for (let i = 0; i < numberOfCells; i++) {
		[operation, arg1, arg2] = readline().split(' ');
		data.push(new Cell(operation, arg1, arg2));
	}

	// Write an action using console.log()
	// To debug: console.error('Debug messages...');
	data.forEach(v => console.log(getValue(v).toString()));
})();