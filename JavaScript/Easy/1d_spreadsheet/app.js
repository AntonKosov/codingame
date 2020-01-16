// https://www.codingame.com/training/easy/1d-spreadsheet

const codinggame_1d_spreadsheet = {
	run: (input, output) => {
		class Cell {
			constructor(operation, first, second) {
				this.operation = operation;
				this.first = first;
				this.second = second;
				this.value = undefined;
			}
		}
	
		const getValue = cell => {
			if (cell.value === undefined) {
				cell.value = operations[cell.operation](cell);
			}
	
			return cell.value;
		};
	
		const argValue = arg => {
			let value = 0;
	
			if (arg[0] === '$') {
				const cellIndex = arg.substring(1);
				value = parseInt(getValue(data[cellIndex]));
			} else {
				value = parseInt(arg);
			}
	
			return value;
		};
	
		const operations = {
			"VALUE": cell => argValue(cell.first),
			"ADD": cell => argValue(cell.first) + argValue(cell.second),
			"SUB": cell => argValue(cell.first) - argValue(cell.second),
			"MULT": cell => argValue(cell.first) * argValue(cell.second)
		};
	
		const data = [];
		const numberOfCells = parseInt(input());
		for (let i = 0; i < numberOfCells; i++) {
			const [operation, arg1, arg2] = input().split(' ');
			data.push(new Cell(operation, arg1, arg2));
		}
	
		// Write an action using console.log()
		// To debug: console.error('Debug messages...');
		data.forEach(v => output(getValue(v).toString()));
	}
};

if (typeof readline !== 'undefined') {
	codinggame_1d_spreadsheet.run(readline, console.log);
}

module.exports = codinggame_1d_spreadsheet;