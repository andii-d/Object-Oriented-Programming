public class TypingRaceRunner {
     public static void main(String[] args) {
         TypingRace race = new TypingRace(40);
         race.addTypist(new Typist('1', "TURBOFINGERS", 0.85), 1);
         race.addTypist(new Typist('2', "QWERTY_QUEEN", 0.60), 2);
         race.addTypist(new Typist('3', "HUNT_N_PECK", 0.30), 3);
         race.startRace();
     }
 }