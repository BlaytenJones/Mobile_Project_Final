package edu.uark.ahnelson.openstreetmap2024.MapsActivity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.*
import kotlinx.coroutines.launch
import edu.uark.ahnelson.openstreetmap2024.R

class InventoryActivity : AppCompatActivity() {

    private val tokenViewModel: TokenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.inventory_activity)

        // Example: Adding a token (fixing the issue here)
        val token = Token(token = "Token1") // Correct usage, only pass 'token' value
        tokenViewModel.addToken(token)

        // Example: Retrieve and display all tokens
        tokenViewModel.getAllTokens().observe(this) { tokens ->
            tokens.forEach {
                Toast.makeText(this, it.token, Toast.LENGTH_SHORT).show()
            }
        }
    }
}

// Entity representing a token in the database
@Entity(tableName = "tokens")
data class Token(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, // Auto-generated, no need to pass this
    @ColumnInfo(name = "token") val token: String  // The actual token value
)

// DAO (Data Access Object) to interact with the tokens table
@Dao
interface TokenDao {
    @Insert
    suspend fun addToken(token: Token)

    @Query("SELECT * FROM tokens")
    fun getAllTokens(): LiveData<List<Token>>  // This should be fine
}

// RoomDatabase that provides the DAO for interacting with the database
@Database(entities = [Token::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tokenDao(): TokenDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "token_database"
                )
                    .fallbackToDestructiveMigration() // Force recreation of the database if schema changes
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// ViewModel to manage the token data
class TokenViewModel(application: android.app.Application) : AndroidViewModel(application) {
    private val tokenDao: TokenDao = AppDatabase.getDatabase(application).tokenDao()
    private val allTokens: LiveData<List<Token>> = tokenDao.getAllTokens()

    fun addToken(token: Token) {
        viewModelScope.launch {
            tokenDao.addToken(token)
        }
    }

    fun getAllTokens(): LiveData<List<Token>> {
        return allTokens
    }
}
