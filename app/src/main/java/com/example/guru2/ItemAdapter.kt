    package com.example.guru2

    import android.content.Context
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.ArrayAdapter
    import android.widget.Button
    import android.widget.CheckBox
    import android.widget.EditText
    import android.widget.ImageButton
    import android.widget.Spinner
    import android.widget.TextView
    import android.widget.ToggleButton
    import androidx.appcompat.app.AlertDialog
    import androidx.recyclerview.widget.RecyclerView

    // ItemAdapter 클래스 : RecyclerView.Adapter를 상속받아 리스트 아이템을 관리하는 어댑터 클래스
    class ItemAdapter(private val items: MutableList<Item>, private val dbHelper: DatabaseHelper) :
        RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

        // 각 아이템을 위한 ViewHolder를 생성
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
            return ItemViewHolder(view)
        }

        // ViewHolder에 데이터를 바인딩
        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val item = items[position]
            holder.bind(item)

            // 수정 버튼 클릭 시 수정 다이얼로그 표시
            holder.editButton.setOnClickListener {
                showEditDialog(holder.itemView.context, item, position)
            }

            // ToggleButton 상태 변경 시 아이템의 isCompleted 값을 업데이트
            holder.toggleButton.setOnCheckedChangeListener { _, isChecked ->
                item.isCompleted = isChecked
                dbHelper.updateItem(item) // 데이터베이스에 업데이트 반영
            }
        }

        // 아이템의 개수를 반환
        override fun getItemCount(): Int = items.size

        // 새 아이템 리스트로 RecyclerView를 갱신하는 함수
        fun updateList(newList: MutableList<Item>) {
            items.clear()  // 기존 아이템을 삭제
            items.addAll(newList) // 새 아이템 추가
            notifyDataSetChanged() // RecyclerView 갱신
        }

        // 물품 수정/삭제 다이얼로그 표시
        private fun showEditDialog(context: Context, item: Item, position: Int) {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_item, null)
            val itemName = dialogView.findViewById<EditText>(R.id.et_item_name)
            val categorySpinner = dialogView.findViewById<Spinner>(R.id.spinner_category)
            val quantity = dialogView.findViewById<EditText>(R.id.et_quantity)
            val memo = dialogView.findViewById<EditText>(R.id.et_memo)
            val editButton = dialogView.findViewById<Button>(R.id.btn_edit)
            val deleteButton = dialogView.findViewById<Button>(R.id.btn_delte)

            // 다이얼로그에 기존 값 세팅
            itemName.setText(item.name)
            memo.setText(item.memo)
            quantity.setText(item.quantity.toString())

            // 카테고리 스피너 설정
            ArrayAdapter.createFromResource(
                context,
                R.array.categories_array,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                categorySpinner.adapter = adapter
                categorySpinner.setSelection(adapter.getPosition(item.category))
            }

            // 다이얼로그 생성
            val dialog = AlertDialog.Builder(context)
                .setTitle("물품 수정/삭제")
                .setView(dialogView)
                .create()

            // 수정 버튼 클릭 시, 변경된 정보를 저장하고 DB 업데이트
                editButton.setOnClickListener {
                    val newName = itemName.text.toString()
                    val newCategory = categorySpinner.selectedItem.toString()
                    val newQuantity = quantity.text.toString().toIntOrNull() ?: 0
                    val newMemo = memo.text.toString()

                    item.name = newName
                    item.category = newCategory
                    item.quantity = newQuantity
                    item.memo = newMemo

                    dbHelper.updateItem(item)  // 데이터베이스 업데이트
                    notifyItemChanged(position) // RecyclerView에 변화 반영
                    dialog.dismiss() // 다이얼로그 닫기
                }

            // 삭제 버튼 클릭 시, 아이템 삭제 처리
                deleteButton.setOnClickListener {
                    // 삭제 처리
                    dbHelper.deleteItem(item.id) // 데이터베이스에서 아이템 삭제
                    items.removeAt(position)    // 리스트에서 아이템 삭제
                    notifyItemRemoved(position) // RecyclerView에서 아이템 삭제
                    dialog.dismiss()            // 다이얼로그 닫기
                }

            dialog.show() // 다이얼로그 표시

        }

        // ViewHolder 클래스: RecyclerView의 각 아이템을 관리하는 클래스
        class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val toggleButton: ToggleButton = view.findViewById(R.id.toggleButton_item)
            val name: TextView = view.findViewById(R.id.tv_item_name)
            val category: TextView = view.findViewById(R.id.tv_item_category)
            val quantity: TextView = view.findViewById(R.id.tv_item_quantity)
            val memo : TextView = view.findViewById(R.id.tv_item_memo)
            val editButton: ImageButton = view.findViewById(R.id.btn_edit)

            // 아이템 데이터를 뷰에 바인딩
            fun bind(item: Item) {
                name.text = item.name
                category.text = item.category
                quantity.text = "${item.quantity}개"
                memo.text = item.memo
                toggleButton.isChecked = item.isCompleted
            }
        }
    }
