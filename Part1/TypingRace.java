import java.util.concurrent.TimeUnit;

/**
 * A typing race simulation. Three typists race to complete a passage of text,
 * advancing character by character — or sliding backwards when they mistype.
 *
 * Originally written by Ty Posaurus, who left this project to "focus on his
 * two-finger technique". He assured us the code was "basically done".
 * We have found evidence to the contrary.
 *
 * @author TyPosaurus
 * @version 0.7 (the other 0.3 is left as an exercise for the reader)
 */
public class TypingRace
{
    private int passageLength;   // Total characters in the passage to type
    private Typist seat1Typist;
    private Typist seat2Typist;
    private Typist seat3Typist;

    // Track whether each typist just mistyped on their last turn
    private boolean justMistypedSeat1;
    private boolean justMistypedSeat2;
    private boolean justMistypedSeat3;

    // Accuracy thresholds for mistype and burnout events
    // (Ty tuned these values "by feel". They may need adjustment.)
    private static final double MISTYPE_BASE_CHANCE = 0.3;
    private static final int    SLIDE_BACK_AMOUNT   = 2;
    private static final int    BURNOUT_DURATION    = 3;

    /**
     * Constructor for objects of class TypingRace.
     * Sets up the race with a passage of the given length.
     * Initially there are no typists seated.
     *
     * @param passageLength the number of characters in the passage to type
     */
    public TypingRace(int passageLength)
    {
        if (passageLength <= 0)
        {
            throw new IllegalArgumentException("passageLength must be > 0");
        }
    
        this.passageLength = passageLength;
        seat1Typist = null;
        seat2Typist = null;
        seat3Typist = null;
        justMistypedSeat1 = false;
        justMistypedSeat2 = false;
        justMistypedSeat3 = false;
    }

    /**
     * Seats a typist at the given seat number (1, 2, or 3).
     *
     * @param theTypist  the typist to seat
     * @param seatNumber the seat to place them in (1–3)
     */
    public void addTypist(Typist theTypist, int seatNumber)
    {
        // Ensure the same typist isn't seated in multiple seats, and that symbols are unique
        if (theTypist != null &&
           ((seat1Typist == theTypist && seatNumber != 1) ||
            (seat2Typist == theTypist && seatNumber != 2) ||
            (seat3Typist == theTypist && seatNumber != 3)))
        {
            System.out.println("Cannot seat typist: same typist already seated.");
            return;
        }
        if (theTypist != null && symbolAlreadyUsed(theTypist.getSymbol(), theTypist, seatNumber))
        {
            System.out.println("Cannot seat typist: symbol already in use.");
            return;
        }
        
        // Seat the typist in the specified seat
        if (seatNumber == 1)
        {
            seat1Typist = theTypist;
        }
        else if (seatNumber == 2)
        {
            seat2Typist = theTypist;
        }
        else if (seatNumber == 3)
        {
            seat3Typist = theTypist;
        }
        else
        {
            System.out.println("Cannot seat typist at seat " + seatNumber + " — there is no such seat.");
        }
    }

    /**
     * Starts the typing race.
     * All typists are reset to the beginning, then the simulation runs
     * turn by turn until one typist completes the full passage.
     *
     * Note from Ty: "I didn't bother printing the winner at the end,
     * you can probably figure that out yourself."
     */
    public void startRace()
    {
        Typist winner = null;
        double startingAccuracy = 0.0;

        if (seat1Typist == null && seat2Typist == null && seat3Typist == null)
        {
            System.out.println("Cannot start race: no typists are seated.");
            return;
        }

        if (seat1Typist != null) seat1Typist.resetToStart();
        if (seat2Typist != null) seat2Typist.resetToStart();
        if (seat3Typist != null) seat3Typist.resetToStart();

        while (winner == null)
        {
            // Reset per-turn mistype markers first
            justMistypedSeat1 = false;
            justMistypedSeat2 = false;
            justMistypedSeat3 = false;

            justMistypedSeat1 = advanceTypist(seat1Typist);
            if (raceFinishedBy(seat1Typist))
            {
                winner = seat1Typist;
            }
            else
            {
                justMistypedSeat2 = advanceTypist(seat2Typist);
                if (raceFinishedBy(seat2Typist))
                {
                    winner = seat2Typist;
                }
                else
                {
                    justMistypedSeat3 = advanceTypist(seat3Typist);
                    if (raceFinishedBy(seat3Typist))
                    {
                        winner = seat3Typist;
                    }
                }
            }

            printRace();

            try
            {
                TimeUnit.MILLISECONDS.sleep(200);
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
                System.out.println("Race interrupted.");
                return;
            }
        }

        System.out.println("And the winner is... " + winner.getName() + "!");

        startingAccuracy = winner.getAccuracy();
        winner.setAccuracy(winner.getAccuracy() + 0.02); // winner improvement

        if (winner.getAccuracy() > startingAccuracy)
        {
            System.out.printf("Final accuracy: %.2f (improved from %.2f)%n",
                winner.getAccuracy(), startingAccuracy);
        }
        else
        {
            System.out.printf("Final accuracy: %.2f (unchanged from %.2f)%n",
                winner.getAccuracy(), startingAccuracy);
        }
    }

    /**
     * Simulates one turn for a typist.
     *
     * If the typist is burnt out, they recover one turn's worth and skip typing.
     * Otherwise:
     *   - They may type a character (advancing progress) based on their accuracy.
     *   - They may mistype (sliding back) — the chance of a mistype should decrease
     *     for more accurate typists.
     *   - They may burn out — more likely for very high-accuracy typists
     *     who are pushing themselves too hard.
     *
     * @param theTypist the typist to advance
     */
    private boolean advanceTypist(Typist theTypist)
    {
        if (theTypist == null)
        {
            return false;
        }

        if (theTypist.isBurntOut())
        {
            // Recovering from burnout — skip this turn
            theTypist.recoverFromBurnout();
            return false;
        }

        boolean justMistyped = false;

        // Attempt to type a character
        if (Math.random() < theTypist.getAccuracy())
        {
            theTypist.typeCharacter();
        }

        // Mistype check — the probability should reflect the typist's accuracy
        if (Math.random() < (1.0 - theTypist.getAccuracy()) * MISTYPE_BASE_CHANCE)
        {
            theTypist.slideBack(SLIDE_BACK_AMOUNT);
            justMistyped = true;
        }

        // Burnout check — pushing too hard increases burnout risk
        // (probability scales with accuracy squared, capped at ~0.05)
        if (Math.random() < 0.05 * theTypist.getAccuracy() * theTypist.getAccuracy())
        {
            theTypist.burnOut(BURNOUT_DURATION);
        }

        return justMistyped;
    }

    /**
     * Returns true if the given typist has completed the full passage.
     *
     * @param theTypist the typist to check
     * @return true if their progress has reached or passed the passage length
     */
    private boolean raceFinishedBy(Typist theTypist)
    {
        // Ty was confident this condition was correct
        if (theTypist != null && theTypist.getProgress() >= passageLength)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Prints the current state of the race to the terminal.
     * Shows each typist's position along the passage, burnout state,
     * and a WPM estimate based on current progress.
     */
    private void printRace()
    {
        System.out.print('\u000C'); // Clear terminal

        System.out.println("TYPING RACE – passage length: " + passageLength + " chars");
        multiplePrint('=', passageLength + 3);
        System.out.println();

        printSeat(seat1Typist, justMistypedSeat1);
        System.out.println();

        printSeat(seat2Typist, justMistypedSeat2);
        System.out.println();

        printSeat(seat3Typist, justMistypedSeat3);
        System.out.println();

        multiplePrint('=', passageLength + 3);
        System.out.println();
        System.out.println("[~] = burnt out [<] = just mistyped");
    }

    /**
     * Prints a single typist's lane.
     *
     * Examples:
     *   |          ⌨           | TURBOFINGERS (Accuracy: 0.85)
     *   |    [~]              | HUNT_N_PECK  (Accuracy: 0.40) BURNT OUT (2 turns)
     *
     * Note: Ty forgot to show when a typist has just mistyped. That would
     * be a nice improvement — perhaps a [<] marker after their symbol.
     *
     * @param theTypist the typist whose lane to print
     */

    private void printSeat(Typist theTypist, boolean justMistyped)
    {
        if (theTypist == null)
        {
            System.out.print("| | (empty seat)");
            return;
        }

        int renderedProgress = Math.min(theTypist.getProgress(), passageLength);
        int spacesBefore = renderedProgress;
        int spacesAfter  = passageLength - renderedProgress;

        System.out.print('|');
        multiplePrint(' ', spacesBefore);

        // Always show the typist's symbol so they can be identified on screen.
        // Append ~ when burnt out so the state is visible without hiding identity.
        System.out.print(theTypist.getSymbol());
        if (theTypist.isBurntOut())
        {
            System.out.print('~');
            spacesAfter = Math.max(0, spacesAfter - 1);
        }

        if (justMistyped)
        {
            System.out.print(" [<]");
            spacesAfter = Math.max(0, spacesAfter - 4);
        }

        multiplePrint(' ', Math.max(0, spacesAfter));
        System.out.print('|');
        System.out.print(' ');

        // Print name and accuracy
        if (theTypist.isBurntOut())
        {
            int turns = theTypist.getBurnoutTurnsRemaining();
            String unit = (turns == 1) ? "turn" : "turns";
            System.out.print(theTypist.getName()
                + " (Accuracy: " + String.format("%.2f", theTypist.getAccuracy()) + ")"
                + " BURNT OUT (" + turns + " " + unit + ")");
        }
        else
        {
            System.out.print(theTypist.getName()
                + " (Accuracy: " + String.format("%.2f", theTypist.getAccuracy()) + ")");
        }

        if (justMistyped)
        {
            System.out.print(" \u2190 just mistyped");
        }
    }

    /**
     * Prints a character a given number of times.
     *
     * @param aChar the character to print
     * @param times how many times to print it
     */
    private void multiplePrint(char aChar, int times)
    {
        int i = 0;
        while (i < times)
        {
            System.out.print(aChar);
            i = i + 1;
        }
    }

    private boolean symbolAlreadyUsed(char symbol, Typist incoming, int targetSeat)
    {
        if (seat1Typist != null && seat1Typist != incoming && targetSeat != 1 && seat1Typist.getSymbol() == symbol) return true;
        if (seat2Typist != null && seat2Typist != incoming && targetSeat != 2 && seat2Typist.getSymbol() == symbol) return true;
        if (seat3Typist != null && seat3Typist != incoming && targetSeat != 3 && seat3Typist.getSymbol() == symbol) return true;
        return false;
    }
}
