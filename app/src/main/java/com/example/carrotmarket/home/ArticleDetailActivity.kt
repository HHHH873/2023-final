package com.example.carrotmarket.home

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.carrotmarket.DBKey.Companion.CHILD_CHAT
import com.example.carrotmarket.DBKey.Companion.DB_USER
import com.example.carrotmarket.R
import com.example.carrotmarket.chatlist.ChatListItem
import com.example.carrotmarket.home.ArticleModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class ArticleDetailActivity : AppCompatActivity() {

    private lateinit var articleDB: DatabaseReference
    private lateinit var userDB: DatabaseReference
    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        articleDB = FirebaseDatabase.getInstance().getReference("Articles")
        userDB = Firebase.database.reference.child(DB_USER)

        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            // 현재 액티비티를 종료하고 이전 화면(여기서는 HomeFragment가 있는 화면)으로 돌아감
            finish()
        }

        val chatButton = findViewById<Button>(R.id.button)
        chatButton.setOnClickListener {
            // 현재 게시글 정보를 가져옵니다.
            val articleCreatedAt = intent.getLongExtra("articleCreatedAt", -1)
            if (articleCreatedAt != -1L) {
                createChatRoom(articleCreatedAt)
            }
        }

        val articleCreatedAt = intent.getLongExtra("articleCreatedAt", -1)
        if (articleCreatedAt != -1L) {
            loadArticleDetails(articleCreatedAt)
        }
    }

    private fun loadArticleDetails(createdAt: Long) {
        articleDB.orderByChild("createdAt").equalTo(createdAt.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val articleSnapshot = snapshot.children.first()
                        //val articleId = articleSnapshot.key // 게시물의 ID
                        val article = articleSnapshot.getValue(ArticleModel::class.java)
                        article ?: return

                        // UI 컴포넌트에 데이터 연결
                        updateUIWithArticleData(article)

                        val editButton = findViewById<Button>(R.id.editButton)
                        editButton.setOnClickListener {
                            // 현재 사용자가 게시글의 작성자인 경우에만 편집 다이얼로그를 표시
                            if (auth.currentUser != null && auth.currentUser!!.uid == article.sellerId) {
                                showEditDialog(article)
                            } else {
                                // 사용자가 게시글의 작성자가 아닌 경우 스낵바 표시
                                Snackbar.make(findViewById(R.id.constLayout), "내가 작성한 게시글이 아닙니다", Snackbar.LENGTH_LONG).show()
                            }
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // 오류 처리
                }
            })
    }

    private fun updateUIWithArticleData(article: ArticleModel) {
        // 이미지 URL 업데이트
        val imageView = findViewById<ImageView>(R.id.detailImage)
        Glide.with(this).load(article.imageURL).into(imageView)

        // 판매 상태 업데이트
        val sellStateTextView = findViewById<TextView>(R.id.sellState)
        sellStateTextView.text = if (article.onsale) "판매중" else "판매완료"

        // 판매자 닉네임 업데이트
        val sellerNickName = findViewById<TextView>(R.id.nickname)
        sellerNickName.text = article.sellerId

        // 제목 업데이트
        val titleTextView = findViewById<TextView>(R.id.detailTitle)
        titleTextView.text = article.title

        // 상세 설명 업데이트
        val contentTextView = findViewById<TextView>(R.id.detailContent)
        contentTextView.text = article.detailDescription

        // 가격 업데이트
        val priceTextView = findViewById<TextView>(R.id.price)
        priceTextView.text = "${article.price}원"
    }

    private fun createChatRoom(articleCreatedAt: Long) {
        // 게시글 정보를 가져오는 로직...
        articleDB.orderByChild("createdAt").equalTo(articleCreatedAt.toDouble())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val article = snapshot.children.first().getValue(ArticleModel::class.java)
                        article ?: return

                        // 채팅방 생성 로직
                        if (auth.currentUser != null && auth.currentUser!!.uid != article.sellerId) {
                            val chatRoom = ChatListItem(
                                buyerId = auth.currentUser!!.uid,
                                sellerId = article.sellerId,
                                itemTitle = article.title,
                                key = System.currentTimeMillis()
                            )

                            userDB.child(auth.currentUser!!.uid)
                                .child(CHILD_CHAT)
                                .push()
                                .setValue(chatRoom)

                            userDB.child(article.sellerId)
                                .child(CHILD_CHAT)
                                .push()
                                .setValue(chatRoom)

                            Snackbar.make(findViewById(R.id.constLayout), "채팅방이 생성되었습니다. 채팅탭에서 확인해주세요.", Snackbar.LENGTH_LONG).show()
                        } else {
                            Snackbar.make(findViewById(R.id.constLayout), "내가 올린 아이템 입니다.", Snackbar.LENGTH_LONG).show()
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // 오류 처리
                }
            })
        }

    private fun showEditDialog(article: ArticleModel) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_article, null)
        val priceEditText = dialogView.findViewById<EditText>(R.id.editTextPrice)
        val saleStatusSpinner = dialogView.findViewById<Spinner>(R.id.spinnerSaleStatus)

        val saleStatusAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.sale_status_array,
            android.R.layout.simple_spinner_item
        )
        saleStatusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        saleStatusSpinner.adapter = saleStatusAdapter

        // 현재 값으로 초기화
        priceEditText.setText(article.price)
        saleStatusSpinner.setSelection(if (article.onsale) 0 else 1)

        AlertDialog.Builder(this)
            .setTitle("게시글 수정")
            .setView(dialogView)
            .setPositiveButton("수정") { dialog, _ ->
                val newPrice = priceEditText.text.toString()
                val newSaleStatus = saleStatusSpinner.selectedItemPosition == 0

                // DB 업데이트
                updateArticleInDB(article.createdAt, newPrice, newSaleStatus)
                dialog.dismiss()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun updateArticleInDB(createdAt: Long, newPrice: String, newSaleStatus: Boolean) {
        articleDB.orderByChild("createdAt").equalTo(createdAt.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // 검색된 게시글의 ID 찾기
                        val articleSnapshot = snapshot.children.first()
                        val articleId = articleSnapshot.key ?: return

                        // 업데이트할 데이터 맵 생성
                        val articleUpdateMap = mapOf(
                            "price" to newPrice,
                            "onsale" to newSaleStatus
                        )

                        // 게시글 업데이트
                        articleDB.child(articleId).updateChildren(articleUpdateMap)
                            .addOnSuccessListener {
                                // 업데이트 성공 시 처리
                                loadArticleDetails(createdAt)
                            }
                            .addOnFailureListener { exception ->
                                // 업데이트 실패 시 로그 남기기
                                Log.e("ArticleDetailActivity", "Failed to update article: ", exception)
                            }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // 오류 처리
                }
            })
    }


}

