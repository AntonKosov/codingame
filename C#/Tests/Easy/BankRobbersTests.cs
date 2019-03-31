using System.Collections.Generic;
using CodinGame.Easy;
using Xunit;

namespace CodinGame.Tests.Easy
{
    public class BankRobbersTests
    {
        [Fact]
        public void OneRobberOneDigitOneCharacterOneVault()
        {
            var vaults = new List<BankRobbers.Vault>
            {
                new BankRobbers.Vault(2, 1)
            };
            var bankRobbers = new BankRobbers(1, vaults);
            
            Assert.Equal(50, bankRobbers.CalculateTime());
        }

        [Fact]
        public void OneRobberZeroDigitOneCharacterOneVault()
        {
            var vaults = new List<BankRobbers.Vault>
            {
                new BankRobbers.Vault(1, 0)
            };
            var bankRobbers = new BankRobbers(1, vaults);
            
            Assert.Equal(5, bankRobbers.CalculateTime());
        }

        [Fact]
        public void OneRobberOneDigitZeroCharacterOneVault()
        {
            var vaults = new List<BankRobbers.Vault>
            {
                new BankRobbers.Vault(1, 1)
            };
            var bankRobbers = new BankRobbers(1, vaults);
            
            Assert.Equal(10, bankRobbers.CalculateTime());
        }

        [Fact]
        public void OneRobberTwoDigitThreeCharacterOneVault()
        {
            var vaults = new List<BankRobbers.Vault>
            {
                new BankRobbers.Vault(5, 2)
            };
            var bankRobbers = new BankRobbers(1, vaults);
            
            Assert.Equal(100 * 125, bankRobbers.CalculateTime());
        }

        [Fact]
        public void OneRobberTwoVaults()
        {
            var vaults = new List<BankRobbers.Vault>
            {
                new BankRobbers.Vault(1, 1), // 1 digit
                new BankRobbers.Vault(1, 0) // 1 character
            };
            var bankRobbers = new BankRobbers(1, vaults);
            
            Assert.Equal(10 + 5, bankRobbers.CalculateTime());
        }

        [Fact]
        public void TwoRobberThreeVaults()
        {
            var vaults = new List<BankRobbers.Vault>
            {
                new BankRobbers.Vault(1, 1), // 1 digit
                new BankRobbers.Vault(1, 0), // 1 character - 1
                new BankRobbers.Vault(2, 1)  // 1 digit + 1 character - 1
            };
            var bankRobbers = new BankRobbers(2, vaults);
            
            Assert.Equal(5 + 50, bankRobbers.CalculateTime());
        }

        [Fact]
        public void ThreeRobberFiveVaults()
        {
            var vaults = new List<BankRobbers.Vault>
            {
                new BankRobbers.Vault(1, 0), // 1 character - 0
                new BankRobbers.Vault(2, 1), // 1 digit + 1 character - 1
                new BankRobbers.Vault(1, 1), // 1 digit - 2
                
                new BankRobbers.Vault(3, 1), // 0
                new BankRobbers.Vault(1, 0)  // 2
            };
            var bankRobbers = new BankRobbers(3, vaults);
            
            Assert.Equal(5 + 250, bankRobbers.CalculateTime());
        }
    }
}