package com.example.tommyho_michellesun_camillechristie_hangman

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import com.example.tommyho_michellesun_camillechristie_hangman.ui.theme.TommyHoMichelleSunCamilleChristieHangmanTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TommyHoMichelleSunCamilleChristieHangmanTheme {
                Column {
                    zeGame()
                }
            }
        }
    }
}

@Composable
fun loadImg(incorrects: MutableState<Int>, modifier: Modifier) {
    when(incorrects.value){
        0 -> Image(painter = painterResource(id = R.drawable.kirb1), contentDescription = "It a Kirby")
        1 -> Image(painter = painterResource(id = R.drawable.kirb2), contentDescription = "It a Kirby")
        2 -> Image(painter = painterResource(id = R.drawable.kirb3), contentDescription = "It a Kirby")
        3 -> Image(painter = painterResource(id = R.drawable.kirb4), contentDescription = "It a Kirby")
        4 -> Image(painter = painterResource(id = R.drawable.kirb5), contentDescription = "It a Kirby")
        5 -> Image(painter = painterResource(id = R.drawable.kirb6), contentDescription = "It a Kirby")
        6 -> Image(painter = painterResource(id = R.drawable.kirb7), contentDescription = "It a Kirby")
        7 -> Image(painter = painterResource(id = R.drawable.gameover), contentDescription = "It a Kirby")
    }
}

@Composable
fun zeGame() {
    val configuration = LocalConfiguration.current
    var lettersPicked = rememberSaveable { mutableStateOf(mutableSetOf<Char>()) } // Use MutableState<MutableSet<Char>>
    var incorrects = rememberSaveable { mutableIntStateOf(0) }
    var hintState = rememberSaveable { mutableIntStateOf(0) }
    var hintText = rememberSaveable { mutableStateOf(false) }
    var word = rememberSaveable { mutableStateOf("ability") }
    var resetTriggered by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        when (configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                Row(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.weight(0.4f)) {
                        LetterButtons(lettersPicked, word, incorrects, hintState, hintText, resetTriggered, modifier = Modifier.weight(0.3f))
                        HintContent(hintState, hintText, incorrects, word.value, lettersPicked)
                    }
                    Box(modifier = Modifier.weight(0.6f)) {
                        GamePlayScreen(word, lettersPicked, incorrects)
                    }
                }
            }
            else -> {
                Box(modifier = Modifier.weight(0.6f)) {
                    GamePlayScreen(word, lettersPicked, incorrects)
                }
                LetterButtons(lettersPicked, word, incorrects, hintState, hintText, resetTriggered, modifier = Modifier.weight(0.3f))
            }
        }

        Button(
            onClick = {
                rest(lettersPicked, incorrects, hintState, hintText, word)
                resetTriggered = true  // Trigger recomposition
                resetTriggered = false // Reset immediately after triggering
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("New Game")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LetterButtons(
    lettersPicked: MutableState<MutableSet<Char>>,
    word: MutableState<String>,
    incorrects: MutableIntState,
    hintState: MutableIntState,
    hintText: MutableState<Boolean>,
    resetTriggered: Boolean,
    modifier: Modifier
) {
    val alphabet = ('a'..'z').toList()
    val scope = rememberCoroutineScope()
    var correctness by remember { mutableStateOf(false) }
    var gameWin by remember { mutableStateOf(false) }
    var gameLose by remember { mutableStateOf(false) }
    val snackBarHostState = remember { SnackbarHostState() }
    var snackMessage by remember { mutableStateOf("") }

    Column {
        FlowRow {
            alphabet.forEach { letter ->
                Button(
                    onClick = {
                        lettersPicked.value = lettersPicked.value.toMutableSet().apply { add(letter) } // Create a new MutableSet
                        if (!word.value.contains(letter)) {
                            correctness = true
                            incorrects.intValue++
                            if (incorrects.intValue == 7) {
                                gameLose = true
                            }
                            snackMessage = if (gameLose) "U LOSE. LAAAAAAAAAME pls reset" else "WRONG. that was wrong."
                        } else {
                            correctness = true
                            gameWin = word.value.all { lettersPicked.value.contains(it) }
                            snackMessage = if (gameWin) "WINWINWINWIWNIWNIWNWIWNIn reset it please :>" else "true girlypop u go gurl"
                        }

                        // Snackbar logic
                        scope.launch {
                            val result = snackBarHostState.showSnackbar(
                                message = snackMessage,
                                actionLabel = "Dismiss",
                                duration = SnackbarDuration.Indefinite
                            )
                            when (result) {
                                SnackbarResult.ActionPerformed -> {
                                    correctness = false
                                    if (gameWin || gameLose) {
                                        rest(lettersPicked, incorrects, hintState, hintText, word)
                                        gameWin = false
                                        gameLose = false
                                    }
                                }
                                SnackbarResult.Dismissed -> {
                                    correctness = false
                                    if (gameWin || gameLose) {
                                        rest(lettersPicked, incorrects, hintState, hintText, word)
                                        gameWin = false
                                        gameLose = false
                                    }
                                }
                            }
                        }
                    },
                    enabled = !lettersPicked.value.contains(letter) && !resetTriggered
                ) {
                    Text(text = letter.toString())
                }
            }
        }
        SnackbarHost(hostState = snackBarHostState)
    }
}

@Composable
fun GamePlayScreen(
    word: MutableState<String>,
    lettersPicked: MutableState<MutableSet<Char>>,
    incorrects: MutableIntState,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        loadImg(incorrects, modifier = Modifier.fillMaxWidth())
        Row {
            word.value.forEach { letter ->
                Card(modifier = Modifier.size(width = 50.dp, height = 50.dp)) {
                    Text(text = if (lettersPicked.value.contains(letter)) letter.toString() else "_")
                }
            }
        }
        Text(text = "Incorrect Guesses: ${incorrects.intValue}")
    }
}

@Composable
fun HintContent(
    hintState: MutableIntState,
    hintText: MutableState<Boolean>,
    incorrects: MutableIntState,
    word: String,
    lettersPicked: MutableState<MutableSet<Char>>
) {
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

    Column {
        Button(onClick = {
            when (hintState.intValue) {
                0 -> {
                    hintText.value = true
                    scope.launch {
                        snackBarHostState.showSnackbar("Hint: The word contains a specific letter")
                    }
                }
                1 -> {
                    // Logic for disabling half of the remaining letters
                    val remainingLetters = ('a'..'z').filter { !lettersPicked.value.contains(it) && !word.contains(it) }
                    val lettersToDisable = remainingLetters.shuffled().take(remainingLetters.size / 2)
                    lettersPicked.value = lettersPicked.value.toMutableSet().apply { addAll(lettersToDisable) }
                    incorrects.intValue++
                    scope.launch {
                        snackBarHostState.showSnackbar("Hint: Half of the remaining incorrect letters have been disabled")
                    }
                }
                2 -> {
                    // Logic for showing all vowels
                    val vowels = setOf('a', 'e', 'i', 'o', 'u')
                    lettersPicked.value = lettersPicked.value.toMutableSet().apply { addAll(vowels) }
                    incorrects.intValue++
                    scope.launch {
                        snackBarHostState.showSnackbar("Hint: All vowels have been revealed")
                    }
                }
                else -> {
                    scope.launch {
                        snackBarHostState.showSnackbar("No more hints available")
                    }
                }
            }
            if (hintState.intValue < 3) {
                hintState.intValue++
            }
        }) {
            Text(text = "Hint ${hintState.intValue + 1}")
        }

        if (hintText.value) {
            val pickedLetter = word.find { !lettersPicked.value.contains(it) }?.toString() ?: ""
            Text(text = "The word contains this letter: $pickedLetter")
        }

        SnackbarHost(hostState = snackBarHostState)
    }
}

fun rest(
    lettersPicked: MutableState<MutableSet<Char>>,
    incorrects: MutableIntState,
    hintState: MutableIntState,
    hintText: MutableState<Boolean>,
    word: MutableState<String>
) {
    lettersPicked.value = mutableSetOf() // Assign a new empty MutableSet
    incorrects.intValue = 0
    hintState.intValue = 0
    hintText.value = false
    word.value = shuffledWords()
}

fun shuffledWords(): String {
    val words = listOf(
        "ability",
        "absence",
        "academy",
        "account",
        "address",
        "advance"
    ).shuffled()

    return words[0]
}
