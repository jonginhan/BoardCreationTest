package player.service;

import player.Player;
import player.SpecialDicePattern;

import java.util.List;

public class PlayerServiceImpl implements PlayerService {

    final int EVEN_ODD_DECISION_VALUE = 2;
    final int EVEN = 0;

    // 1. 일반 주사위를 보고 짝수인지 판정한다.
    // 2. 짝수라면 특수 주사위의 숫자를 파악한다.

    // 2-1. 숫자 4가 나오는 경우 즉시 게임 오버된다.
    // 2-2. 숫자 3이 나오는 경우엔 친구들 주사위에서 점수를 3점씩 뺏어올 수 있다.
    // 2-3. 숫자 5가 나오는 경우엔 자신의 점수를 2점씩 친구들에게 나눠줘야 한다.
    // 2-4. 숫자 1의 경우엔 모두 다 함께 -2점씩 감점된다.

    // 3. 각 숫자에 적합한 작업을 진행한다.
    private Boolean isEven(List<Player> playerList, int currentIdx) {
        int diceNumber = playerList.get(currentIdx).getGeneralDiceNumber();
        Boolean isEven = diceNumber % EVEN_ODD_DECISION_VALUE == EVEN;

        if (isEven) {
            System.out.println("일반 주사위는 짝수입니다: " + diceNumber);
        } else {
            System.out.println("일반 주사위는 홀수입니다: " + diceNumber);
        }

        return isEven;
    }

    private int getSpecialDiceNumber (List<Player> playerList, int currentIdx) {
        return playerList.get(currentIdx).getSpecialDiceNumber();
    }

    private SpecialDicePattern checkSpecialDicePattern (
            int specialDice, List<Player> playerList, int currentIdx) {

        if (specialDice == SpecialDicePattern.PATTERN_DEATH.getValue())
            return SpecialDicePattern.PATTERN_DEATH;

        if (specialDice == SpecialDicePattern.PATTERN_STEAL.getValue())
            return SpecialDicePattern.PATTERN_STEAL;

        if (specialDice == SpecialDicePattern.PATTERN_DONATE.getValue())
            return SpecialDicePattern.PATTERN_DONATE;

        if (specialDice == SpecialDicePattern.PATTERN_BUDDY_FUCKER.getValue())
            return SpecialDicePattern.PATTERN_BUDDY_FUCKER;

        return SpecialDicePattern.PATTERN_NOTHING;
    }

    private int calcTotalDiceNumber (List<Player> playerList, int currentIdx) {
        return playerList.get(currentIdx).getGeneralDiceNumber() +
                playerList.get(currentIdx).getSpecialDiceNumber();
    }

    private int howMuchCanWeSteal (int targetDiceNumber) {
        final int STEAL_SCORE = 3;

        if (targetDiceNumber - STEAL_SCORE >= 0) {
            return STEAL_SCORE;
        } else if (targetDiceNumber - STEAL_SCORE == -3) {
            System.out.println("뺏을 점수가 없음");
            return 0;
        } else {
            return targetDiceNumber;
        }
    }

    private void stealEachPlayerScore (List<Player> playerList, int currentIdx) {

        int myDiceNumber = calcTotalDiceNumber(playerList, currentIdx);

        for (int i = 0; i < playerList.size(); i++) {
            if (i == currentIdx) { continue; }

            int targetDiceNumber = calcTotalDiceNumber(playerList, i);

            myDiceNumber += howMuchCanWeSteal(targetDiceNumber);

            targetDiceNumber -= howMuchCanWeSteal(targetDiceNumber);

            playerList.get(i).setTotalDiceScore(targetDiceNumber);
        }

        playerList.get(currentIdx).setTotalDiceScore(myDiceNumber);
    }

    private void doDonate (int myDiceNumber, List<Player> playerList, int currentIdx) {
        final int DONATE_SCORE = 2;

        for (int i = 0; i < playerList.size(); i++) {
            if (i == currentIdx) { continue; }

            int targetTotalScore = playerList.get(i).getTotalDiceScore();
            int donationValue = 0;

            if (myDiceNumber >= DONATE_SCORE) {
                myDiceNumber -= DONATE_SCORE;
                donationValue = DONATE_SCORE;
            } else if (myDiceNumber > 0) {
                myDiceNumber -= DONATE_SCORE;
                donationValue = 1;
            } else {
                System.out.println("기부 불가");
                donationValue = 0;
                myDiceNumber = 0;
            }

            playerList.get(i).setTotalDiceScore(targetTotalScore + donationValue);
            playerList.get(currentIdx).setTotalDiceScore(myDiceNumber);
        }
    }

    private void donateToEachPlayer (List<Player> playerList, int currentIdx) {
        final int DONATE_SCORE = 2;

        int myDiceNumber = calcTotalDiceNumber(playerList, currentIdx);

        doDonate(myDiceNumber, playerList, currentIdx);
    }

    private void postProcessAfterGetPattern (
            SpecialDicePattern dicePattern,
            List<Player> playerList,
            int currentIdx
    ) {

            //int myDiceNumber = calcTotalDiceNumber(playerList, currentIdx);
            //int targetDiceNumber;

            if (dicePattern == SpecialDicePattern.PATTERN_DEATH)
                ;

            // 상대방의 Dice를 보고 뺏어와야함(없으면 뺏을것이 없는 상태이기도함)
            if (dicePattern == SpecialDicePattern.PATTERN_STEAL)
                stealEachPlayerScore(playerList, currentIdx);

            if (dicePattern == SpecialDicePattern.PATTERN_DONATE)
                donateToEachPlayer(playerList, currentIdx);

            if (dicePattern == SpecialDicePattern.PATTERN_BUDDY_FUCKER)
                minusEachPlayerScore(playerList);

            if (dicePattern == SpecialDicePattern.PATTERN_NOTHING) {
                playerList.get(currentIdx).setTotalDiceScore(
                        calcTotalDiceNumber(playerList, currentIdx));
            }

    }

    private int howMuchCanWeMinus (int targetDiceNumber) {
        final int SCORE_MINUS = 2;

        if (targetDiceNumber - SCORE_MINUS >= 0) {
            return SCORE_MINUS;
        } else if (targetDiceNumber == 0) {
            System.out.println("감소시킬 점수가 없음");
            return 0;
        } else {
            return targetDiceNumber;
        }
    }

    private void minusEachPlayerScore(List<Player> playerList) {
        for (int i = 0; i < playerList.size(); i++) {
            int targetDiceNumber = calcTotalDiceNumber(playerList, i); //플레이어 i 의 점수
            targetDiceNumber -= howMuchCanWeMinus(targetDiceNumber);
            playerList.get(i).setTotalDiceScore(targetDiceNumber);
        }
    }

    private void minusEachPlayerScore1 (List<Player> playerList, int currentIdx) {

        int myDiceNumber = calcTotalDiceNumber(playerList, currentIdx);

        for (int i = 0; i < playerList.size(); i++) {

            int targetDiceNumber = calcTotalDiceNumber(playerList, i);

            myDiceNumber += howMuchCanWeMinus(targetDiceNumber);
        }

        playerList.get(currentIdx).setTotalDiceScore(myDiceNumber);
    }

    private void applyEachPlayer (List<Player> playerList, int currentIdx) {
        /*
        if (isEven(playerList, currentIdx)) {

            int specialDice = getSpecialDiceNumber(playerList, currentIdx);

            SpecialDicePattern dicePattern =
                    checkSpecialDicePattern(specialDice, playerList, currentIdx);

            System.out.println("pattern: " + dicePattern.getName() +
                    "value: " + dicePattern.getValue());

            postProcessAfterGetPattern(dicePattern, playerList, currentIdx);
        }
         */

        int specialDice = getSpecialDiceNumber(playerList, currentIdx);

        SpecialDicePattern dicePattern =
                checkSpecialDicePattern(specialDice, playerList, currentIdx);

        System.out.println("pattern: " + dicePattern.getName() +
                "value: " + dicePattern.getValue());

        postProcessAfterGetPattern(dicePattern, playerList, currentIdx);
    }
    @Override
    public Boolean playDiceGame(List<Player> playerList) {
        for (int i = 0; i < playerList.size(); i++) {
            System.out.println("player" + (i + 1) + ": ");
            applyEachPlayer(playerList, i);
        }

        for (int i = 0; i < playerList.size(); i++) {
            System.out.println("총합: " + playerList.get(i).getTotalDiceScore());
        }

        return false;
    }

    /*

     */
}
