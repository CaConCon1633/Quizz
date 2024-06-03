import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.willow.quiz.Models.Exam


class Questions(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "questions.db"
        private const val TABLE_QUESTIONS = "questions"
        private const val TABLE_ANSWERS = "answers"

        private const val KEY_ID = "id"
        private const val KEY_QUESTION = "question"
        private const val KEY_CORRECT_ANSWER = "correct_answer"

        private const val KEY_ANSWER_ID = "answer_id"
        private const val KEY_ANSWER_TEXT = "answer_text"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createQuestionsTable =
            ("CREATE TABLE $TABLE_QUESTIONS($KEY_ID INTEGER PRIMARY KEY,$KEY_QUESTION TEXT,$KEY_CORRECT_ANSWER INTEGER)")
        val createAnswersTable =
            ("CREATE TABLE $TABLE_ANSWERS($KEY_ANSWER_ID INTEGER PRIMARY KEY,$KEY_ANSWER_TEXT TEXT,$KEY_ID INTEGER, FOREIGN KEY($KEY_ID) REFERENCES $TABLE_QUESTIONS($KEY_ID))")
        db.execSQL(createQuestionsTable)
        db.execSQL(createAnswersTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_QUESTIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ANSWERS")
        onCreate(db)
    }

    fun addQuestion(question: Exam.Question) {
        val db = this.writableDatabase
        try {
            db.beginTransaction()
            val contentValues = ContentValues().apply {
                put(KEY_QUESTION, question.question)
                put(KEY_CORRECT_ANSWER, question.correct)
            }

            val questionId = db.insert(TABLE_QUESTIONS, null, contentValues)

            question.answers?.forEach {
                val answerValues = ContentValues().apply {
                    put(KEY_ANSWER_TEXT, it?.answer)
                    put(KEY_ID, questionId)
                }
                db.insert(TABLE_ANSWERS, null, answerValues)
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e(TAG, "addQuestion: $e")
        } finally {
            db.endTransaction()
        }
    }


    @SuppressLint("Range")
    fun getAllQuestions(): List<Exam.Question> {
        val questionList = ArrayList<Exam.Question>()
        val db = this.readableDatabase
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery("SELECT * FROM $TABLE_QUESTIONS", null)
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val answers = ArrayList<Exam.Question.Answer?>()
                    val questionId = cursor.getInt(cursor.getColumnIndex(KEY_ID))
                    val answerCursor = db.rawQuery("SELECT * FROM $TABLE_ANSWERS WHERE $KEY_ID=$questionId", null)
                    answerCursor?.use {
                        if (it.moveToFirst()) {
                            do {
                                val answer = it.getString(it.getColumnIndex(KEY_ANSWER_TEXT))
                                answers.add(Exam.Question.Answer(answer))
                            } while (it.moveToNext())
                        }
                    }
                    val question = Exam.Question(
                        answers,
                        cursor.getInt(cursor.getColumnIndex(KEY_CORRECT_ANSWER)),
                        cursor.getString(cursor.getColumnIndex(KEY_QUESTION))
                    )
                    questionList.add(question)
                } while (cursor.moveToNext())
            }
        } finally {
            cursor?.close()
            db.close()
        }
        return questionList
    }


    @SuppressLint("Range")
    fun getAnswersForQuestion(questionId: Int): List<String> {
        val db = this.readableDatabase
        val answersList = mutableListOf<String>()

        val selectAnswersQuery = "SELECT $KEY_ANSWER_TEXT FROM $TABLE_ANSWERS WHERE $KEY_ID = ?"
        val selectionArgs = arrayOf(questionId.toString())
        val cursor = db?.rawQuery(selectAnswersQuery, selectionArgs)

        cursor?.use {
            while (it.moveToNext()) {
                val answer = it.getString(it.getColumnIndex(KEY_ANSWER_TEXT))
                answersList.add(answer)
            }
        }
        return answersList

    }

    @SuppressLint("Range")
    fun getCorrectAnswerForQuestion(questionId: Int): Int {
        val db = this.readableDatabase
        var correctAnswer: Int = -1

        val selectCorrectAnswerQuery =
            "SELECT $KEY_CORRECT_ANSWER FROM $TABLE_QUESTIONS WHERE $KEY_ID = ?"
        val selectionArgs = arrayOf(questionId.toString())
        val cursor = db?.rawQuery(selectCorrectAnswerQuery, selectionArgs)

        cursor?.use {
            if (it.moveToFirst()) {
                correctAnswer = it.getInt(it.getColumnIndex(KEY_CORRECT_ANSWER))
            }
        }

        return correctAnswer
    }

    @SuppressLint("Range")
    fun getQuestionById(questionId: Int): String? {
        val db = this.readableDatabase
        var question: String? = null

        val selectQuestionQuery = "SELECT $KEY_QUESTION FROM $TABLE_QUESTIONS WHERE $KEY_ID = ?"
        val selectionArgs = arrayOf(questionId.toString())
        val cursor = db?.rawQuery(selectQuestionQuery, selectionArgs)

        cursor?.use {
            if (it.moveToFirst()) {
                question = it.getString(it.getColumnIndex(KEY_QUESTION))
            }
        }

        return question
    }

    @SuppressLint("Range")
    fun getLastQuestionId(): Int{
        val db = this.readableDatabase
        var lastQuestionId: Int = 0

        val selectLastQuestionIdQuery = "SELECT MAX($KEY_ID) AS last_id FROM $TABLE_QUESTIONS"
        val cursor = db?.rawQuery(selectLastQuestionIdQuery, null)

        cursor?.use {
            if (it.moveToFirst()) {
                lastQuestionId = it.getInt(it.getColumnIndex("last_id"))
            }
        }
        return lastQuestionId
    }
    fun deleteQuestionById(questionId: Int) {
        val db = this.writableDatabase
        try {
            db.beginTransaction()

            db.delete(TABLE_ANSWERS, "$KEY_ID = ?", arrayOf(questionId.toString()))

            db.delete(TABLE_QUESTIONS, "$KEY_ID = ?", arrayOf(questionId.toString()))

            db.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e(TAG, "deleteQuestionById: $e")
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    fun deleteAllDataFromTable() {
        val db = this.writableDatabase
        try {
            db.beginTransaction()
            db.delete(TABLE_ANSWERS, null, null)
            db.delete(TABLE_QUESTIONS, null, null)
            db.setTransactionSuccessful()

        } catch (e: Exception) {
            Log.e(TAG, "deleteAllDataFromTable: $e", )
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    @SuppressLint("Range")
    fun getFirstQuestionId(): Int {
        val db = this.readableDatabase
        var firstQuestionId: Int = 0

        val selectFirstQuestionIdQuery = "SELECT MIN($KEY_ID) AS first_id FROM $TABLE_QUESTIONS"
        val cursor = db?.rawQuery(selectFirstQuestionIdQuery, null)

        cursor?.use {
            if (it.moveToFirst()) {
                firstQuestionId = it.getInt(it.getColumnIndex("first_id"))
            }
        }

        return firstQuestionId
    }

}
