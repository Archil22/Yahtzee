/*
 * File: Yahtzee.java
 * ------------------
 * This program will eventually play the Yahtzee game.
 */
import acm.io.*;
import acm.program.*;
import acm.util.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Yahtzee extends GraphicsProgram implements YahtzeeConstants {
	public static void main(String[] args) {
		new Yahtzee().start(args);
	}
	
	public void run() {
		IODialog dialog = getDialog();
		nPlayers = dialog.readInt("Enter number of players");
		while(!(nPlayers>1&&nPlayers<YahtzeeConstants.MAX_PLAYERS)){
			nPlayers = dialog.readInt("Enter number of players");
		}
		playerNames = new String[nPlayers];
		playerScores = new ArrayList<>();
		for (int i = 1; i <= nPlayers; i++) {
			playerNames[i - 1] = dialog.readLine("Enter name for player " + i);
			playerScores.add(new int[YahtzeeConstants.N_CATEGORIES]);
			setToDefault(playerScores.get(i-1));
		}
		display = new YahtzeeDisplay(getGCanvas(), playerNames);
		playGame();
	}

	//in every player's array of scores are written -1 in place of 13 categories by default
	// in order to limit choosing action of category and distinguish zero score from default array value
	private void setToDefault(int[] scoreArr){
		for (int i = 0; i < scoreArr.length; i++) {
			if(i!=UPPER_BONUS-1 && i!=LOWER_SCORE-1 && i!=UPPER_SCORE-1 && i!=TOTAL-1) {
				scoreArr[i]=-1;
			}
		}
	}

	//for each player in each round in this function is called playerTurn function
	//after finishing game, in this function is called countScore and announceWinner functions
	private void playGame() {
		//number of rounds
		for(int round=1;round<=YahtzeeConstants.N_SCORING_CATEGORIES;round++){
			//player Turn
			for(int player=1;player<=nPlayers;player++){
				display.printMessage(playerNames[player-1]+"'s turn. Click \"Roll Dice\" button to roll the dice.");
				playerTurn(player);
			}
		}
		countScores();
		announceWinner();
	}


	//prints text about the winner
	private void announceWinner(){
		int playerWithMaxTotalNumber = calculateMax();
		display.printMessage("Congretulations,"+playerNames[playerWithMaxTotalNumber]+",you're the winner with total score of "+playerScores.get(playerWithMaxTotalNumber)[TOTAL-1]);
	}

	//calculates maximum TOTAL number and returns winner's index in the player's arrayList
	private int calculateMax() {
		int playerWithMaxScores = 0;
		for(int i=1;i<playerScores.size();i++){
			if(playerScores.get(i)[TOTAL-1]>playerScores.get(playerWithMaxScores)[TOTAL-1]){
				playerWithMaxScores=i;
			}
		}
		return playerWithMaxScores;
	}

	//in that functions is written sequence of actions for each player in each round
	//user clicks roll
	//user choose dices if he wants
	//user selects category
	//scores are written to user's score array in ArrayList
	private void playerTurn(int whichPlayer){
		int[] diceArr = new int[N_DICE];
		//these booleans represents five dices,we generate random numbers for those,which are represented with true
		boolean[] markedDices = new boolean[]{true,true,true,true,true};

		display.waitForPlayerToClickRoll(whichPlayer);
		rollDices(diceArr,markedDices);
		for(int numberOfRolls=1;numberOfRolls<3;numberOfRolls++){
			clearMarks(markedDices);
			display.printMessage("Select the dice you wish to re-roll and click \"Roll Again\"");
			display.waitForPlayerToSelectDice();
			if(anyDiceSelected(markedDices)){
				rollDices(diceArr,markedDices);
			}else {
				break;
			}
		}
		display.printMessage("Select a category of this roll");
		int category = display.waitForPlayerToSelectCategory();
		while(playerScores.get(whichPlayer-1)[category-1]!=-1){
			display.printMessage("This category is already chosen");
			category = display.waitForPlayerToSelectCategory();
		}
		int score=scoreOfCategory(whichPlayer,category,diceArr);
      	playerScores.get(whichPlayer-1)[TOTAL-1]+=score;
		display.updateScorecard(TOTAL,whichPlayer,playerScores.get(whichPlayer-1)[TOTAL-1]);
		playerScores.get(whichPlayer-1)[category-1]=score;
		display.updateScorecard(category,whichPlayer,score);
	}

	//after finishing the game,this function calculates UPPER_SCORE,LOWER_SCORE,UPPER_BONNUS and TOTAL for each player
	private void countScores(){
		for(int player=0;player<playerScores.size();player++){
			int[] playerScore =playerScores.get(player);
			int upperScore=playerScore[UPPER_SCORE-1];
			display.updateScorecard(UPPER_SCORE,player+1,playerScore[UPPER_SCORE-1]);
			if(upperScore>=63){
				playerScore[UPPER_BONUS-1]=35;
			}
			display.updateScorecard(YahtzeeConstants.UPPER_BONUS,player+1,playerScore[UPPER_BONUS-1]);
			int lowerScore=playerScore[LOWER_SCORE-1];
			display.updateScorecard(YahtzeeConstants.LOWER_SCORE,player+1,playerScore[LOWER_SCORE-1]);
			playerScore[TOTAL-1]+=lowerScore;
			display.updateScorecard(YahtzeeConstants.TOTAL,player+1,playerScore[TOTAL-1]);
		}
	}

	//this function returns score for each category
	private int scoreOfCategory(int player,int category,int[] diceArr){
		int score=0;
		switch (category){
			case ONES: score= underSevenCategory(ONES,diceArr);break;
			case TWOS:score = underSevenCategory(TWOS,diceArr);break;
			case THREES:score = underSevenCategory(THREES,diceArr);break;
			case FOURS:score = underSevenCategory(FOURS,diceArr);break;
			case FIVES:score = underSevenCategory(FIVES,diceArr);break;
			case SIXES:score = underSevenCategory(SIXES,diceArr);break;
			case THREE_OF_A_KIND:score = oneKind(THREE_OF_A_KIND,diceArr);break;
			case FOUR_OF_A_KIND:score = oneKind(FOUR_OF_A_KIND,diceArr);break;
			case FULL_HOUSE:score = fullHouse(diceArr);break;
			case SMALL_STRAIGHT:score = straight(SMALL_STRAIGHT,diceArr);break;
			case LARGE_STRAIGHT:score = straight(LARGE_STRAIGHT,diceArr);break;
			case YAHTZEE:score = oneKind(YAHTZEE,diceArr);break;
			case CHANCE:score = chance(diceArr);break;
		}
		if(category<=SIXES){
			playerScores.get(player-1)[UPPER_SCORE-1]+=score;
		}else{
			playerScores.get(player-1)[LOWER_SCORE -1]+=score;
		}

		return score;
	}

	//this function calculates score for categories ONES,TWOS,...SIXES
	private int underSevenCategory(int category,int[] diceArr){
		int score=0;
		for (int j : diceArr) {
			if (j == category) {
				score += category;
			}
		}
		return score;
	}

	//this function calculates score for categories: YAHTZEE, SMALL_STRAIGHT, LARGE_STRAIGHT
	private int oneKind(int category,int[] diceArr){
		int score=0;
		if(checkOneKind(diceArr,category)){
			if(category==YAHTZEE){
				return 50;
			}
			for(int i:diceArr){
				score+=i;
			}
			return score;
		}
		return 0;
	}

	//returns true if dices are suitable for YAHTZEE, THREE_OF_A_KIND or FOUR_OF_A_KIND,
	private boolean checkOneKind(int[] diceArr,int category){
		int maxOccurence=0;
		for (int j : diceArr) {
			if (occurence(j, diceArr) > maxOccurence) {
				maxOccurence = occurence(j, diceArr);
			}
		}
		if(category==THREE_OF_A_KIND&&maxOccurence>=3){
			return true;
		}else if(category==FOUR_OF_A_KIND&&maxOccurence>=4) {
			return true;
		}else return category == YAHTZEE && maxOccurence == 5;
	}

	//calculates occurences for each element in diceArray
	private int occurence(int dice,int[] diceArr){
		int count=0;
		for (int j : diceArr) {
			if (dice == j) {
				count++;
			}
		}
		return count;
	}

	//checks if diceArr is FULL_HOUSE and returns appropriate score
	private int fullHouse(int[] diceArr){
		boolean threeOfThemAreSame = false;
		boolean twoOfThemAreSame = false;
		for(int i : diceArr){
			if(occurence(i,diceArr)==3){
				threeOfThemAreSame=true;
			}else if(occurence(i,diceArr)==2){
				twoOfThemAreSame=true;
			}
		}
		if(twoOfThemAreSame&&threeOfThemAreSame){
			return 25;
		}
		return 0;
	}

	//return score for SMALL_STRAIGHT and LARGE_STRAIGHT
	private int straight(int category,int[] diceArr){
		//if in dice array is number k, in boolean array element at index (k-1) becomes true
		boolean[] checkNumArr = new boolean[6];
		for(int i =0;i<checkNumArr.length;i++){
			if(occurence(i+1,diceArr)>0){
				checkNumArr[i]=true;
			}
		}

		if(category==SMALL_STRAIGHT&&dicesAreSmallStraight(checkNumArr)){
			return 30;
		}else if(category==LARGE_STRAIGHT&&dicesAreLargeStraight(checkNumArr)){
			return 40;
		}
		return 0;
	}

	//return true is dices are SMALL_STRAIGHT
	//checkNum is boolean array and it represents numbers from 1 to 6. if the number is in the dices, checkNum element
	// is true at the index,which is equal to number of dice
	private boolean dicesAreSmallStraight(boolean[] checkNum){
		if(!checkNum[2] || !checkNum[3]){
			return false;
		}else {
			return (checkNum[0] && checkNum[1]) || (checkNum[1] && checkNum[4]) || (checkNum[4] && checkNum[5]);
		}
	}

	//return true is dices are LARGE_STRAIGHT
	//checkNum is boolean array and it represents numbers from 1 to 6. if the number is in the dices, checkNum element
	// is true at the index,which is equal to number of dice
	private boolean dicesAreLargeStraight(boolean[] checkNum){
		if(!checkNum[1] || !checkNum[2] || !checkNum[3] || !checkNum[4]){
			return false;
		}else {
			return checkNum[0] || checkNum[5];
		}
	}

	//returns score for CHANCE category
	private int chance(int[] diceArr){
		int score = 0;
		for (int i : diceArr) {
			score += i;
		}
		return score;
	}

	//this functions sets every element to false in boolean Array
	private void clearMarks(boolean[] markedDices) {
		Arrays.fill(markedDices, false);
	}

	//checks if user selected dices to re-roll and froms boolean array which represents dices
	private boolean anyDiceSelected(boolean[] markedDices){
		boolean isSelected = false;
		for (int i=0;i<5;i++){
			if(display.isDieSelected(i)){
				markedDices[i]=true;
				if(!isSelected) {
					isSelected=true;
				}
			}
		}
		return isSelected;
	}

	//generates random numbers for each dice and updates Yahtzee display
	private void rollDices(int[] diceArr,boolean[] markedDices){
		for(int i=0;i<diceArr.length;i++){
			if(markedDices[i]) {
				diceArr[i]=rgen.nextInt(1,6);
			}
		}
		display.displayDice(diceArr);
	}


	/* Private instance variables */
	private ArrayList<int[]> playerScores;//users and users scores are saved in ArrayList of arrays
	private int nPlayers;
	private String[] playerNames;
	private YahtzeeDisplay display;
	private final RandomGenerator rgen = RandomGenerator.getInstance();
}