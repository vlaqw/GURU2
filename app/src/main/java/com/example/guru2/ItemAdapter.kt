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

    class ItemAdapter(private val items: MutableList<Item>, private val dbHelper: DatabaseHelper) :
        RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
            return ItemViewHolder(view)
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val item = items[position]
            holder.bind(item)

            // 수정 버튼 클릭 이벤트
            holder.editButton.setOnClickListener {
                showEditDialog(holder.itemView.context, item, position)
            }

            // ToggleButton 상태 변경 이벤트
            holder.toggleButton.setOnCheckedChangeListener { _, isChecked ->
                item.isCompleted = isChecked
                dbHelper.updateItem(item)
            }
        }

        override fun getItemCount(): Int = items.size

        private fun showEditDialog(context: Context, item: Item, position: Int) {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_item, null)
            val itemName = dialogView.findViewById<EditText>(R.id.et_item_name)
            val categorySpinner = dialogView.findViewById<Spinner>(R.id.spinner_category)
            val quantity = dialogView.findViewById<EditText>(R.id.et_quantity)
            val memo = dialogView.findViewById<EditText>(R.id.et_memo)
            val editButton = dialogView.findViewById<Button>(R.id.btn_edit)
            val deleteButton = dialogView.findViewById<Button>(R.id.btn_delte)

            //기존 값 설정
            itemName.setText(item.name)
            memo.setText(item.memo)
            quantity.setText(item.quantity.toString())

            //스피너 값 설정
            ArrayAdapter.createFromResource(
                context,
                R.array.categories_array,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                categorySpinner.adapter = adapter
                categorySpinner.setSelection(adapter.getPosition(item.category))
            }

            //다이얼로그 생성
            val dialog = AlertDialog.Builder(context)
                .setTitle("물품 수정/삭제")
                .setView(dialogView)
                .setNegativeButton("취소", null)
                .create()

            // 수정 버튼
                editButton.setOnClickListener {
                    val newName = itemName.text.toString()
                    val newCategory = categorySpinner.selectedItem.toString()
                    val newQuantity = quantity.text.toString().toIntOrNull() ?: 0
                    val newMemo = memo.text.toString()

                    item.name = newName
                    item.category = newCategory
                    item.quantity = newQuantity
                    item.memo = newMemo

                    dbHelper.updateItem(item)
                    notifyItemChanged(position)
                    dialog.dismiss()
                }

            // 삭제 버튼
                deleteButton.setOnClickListener {
                    // 삭제 처리
                    dbHelper.deleteItem(item.id) // 데이터베이스에서 삭제
                    items.removeAt(position)    // 리스트에서 삭제
                    notifyItemRemoved(position) // 어댑터에 알림
                    dialog.dismiss()            // 다이얼로그 닫기
                }

            dialog.show()
        }

        class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val toggleButton: ToggleButton = view.findViewById(R.id.toggleButton_item)
            val name: TextView = view.findViewById(R.id.tv_item_name)
            val category: TextView = view.findViewById(R.id.tv_item_category)
            val quantity: TextView = view.findViewById(R.id.tv_item_quantity)
            val memo : TextView = view.findViewById(R.id.tv_item_memo)
            val editButton: ImageButton = view.findViewById(R.id.btn_edit)

            fun bind(item: Item) {
                name.text = item.name
                category.text = item.category
                quantity.text = "수량: ${item.quantity}개"
                memo.text = item.memo
                toggleButton.isChecked = item.isCompleted
            }
        }
    }
