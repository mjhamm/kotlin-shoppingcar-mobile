package com.example.kotlin_application_2

import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.widget.*
import com.baoyz.swipemenulistview.SwipeMenuCreator
import com.baoyz.swipemenulistview.SwipeMenuItem
import com.baoyz.swipemenulistview.SwipeMenuListView
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.text.*

class MainActivity : AppCompatActivity() {

    private var item: ArrayList<Item> = ArrayList()
    private val mDatabaseHelper = DatabaseHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        populateItemsInDatabase()

        //-----------------------------------------------------------------------------------------------------------------------------------------
        val createBudgetButton: Button = findViewById(R.id.createBudgetButton)
        val goShoppingButton: Button = findViewById(R.id.goShoppingButton)
        val addItemButton: Button = findViewById(R.id.addItemButton)
        val budgetTextBox: EditText = findViewById(R.id.budgetTextBox)
        val itemPriceTextBox: EditText = findViewById(R.id.itemPriceTextBox)
        val productTextBox: EditText = findViewById(R.id.productTextBox)
        val totalBudgetTextView: TextView = findViewById(R.id.totalBudgetTextView)
        val quantityComboBox: Spinner = findViewById(R.id.quantityComboBox)
        val priorityComboBox: Spinner = findViewById(R.id.priorityComboBox)
        val mainListView: SwipeMenuListView = findViewById(R.id.mainListView)

        //-----------------------------------------------------------------------------------------------------------------------------------------

        //ADAPTER FOR MAIN LIST VIEW
        val itemAdapter: ArrayAdapter<Item> = ArrayAdapter(this, android.R.layout.simple_list_item_1, item)
        mainListView.adapter = itemAdapter

        //-----------------------------------------------------------------------------------------------------------------------------------------

        //CREATES PRIORITY AND QUANTITY SPINNERS
        val quantityItems = arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
        val priorityItems = arrayOf("1", "2", "3")
        val quantityAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, quantityItems)
        val priorityAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, priorityItems)
        quantityAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        quantityComboBox.adapter = quantityAdapter
        priorityComboBox.adapter = priorityAdapter
        priorityComboBox.setSelection(2)

        //CREATE BUDGET BUTTON LISTENER
        createBudgetButton.setOnClickListener {
            when {
                budgetTextBox.text.toString().isEmpty() -> toastMessage("Please Enter A Budget.")
                budgetToDouble() <= 0 -> toastMessage("Your Budget Needs To Be Greater Than Zero.")
                else -> {
                    totalBudgetTextView.text = String.format(Locale.US, "%2s", "$" + budgetTextBox.text.toString())
                    //TextView you must use setText not .text because setText has multiple overloads
                    budgetTextBox.setText("")
                }
            }
        }

        //ADD ITEM BUTTON LISTENER
        addItemButton.setOnClickListener {
            when {
                productTextBox.text.toString().isEmpty() -> toastMessage("Please Enter Product.")
                itemPriceTextBox.text.toString().isEmpty() -> toastMessage("Please Enter Price.")
                item.size == 0 -> {
                    addItemToList()
                    itemAdapter.notifyDataSetChanged()
                    productTextBox.setText("")
                    itemPriceTextBox.setText("")
                    quantityComboBox.setSelection(0)
                    priorityComboBox.setSelection(2)
                    getSortedItemByPriority()
                }
                else -> {
                    checkForDuplicates()
                    itemAdapter.notifyDataSetChanged()
                    productTextBox.setText("")
                    itemPriceTextBox.setText("")
                    quantityComboBox.setSelection(0)
                    priorityComboBox.setSelection(2)
                    getSortedItemByPriority()
                }
            }
        }

        //GO SHOPPING BUTTON LISTENER
        goShoppingButton.setOnClickListener {
            when {
                totalBudgetTextView.text.toString().isEmpty() -> toastMessage("You Must Enter A Budget To Go Shopping.")
                item.size == 0 -> toastMessage("You Must Have Items In Your Cart To Go Shopping.")

                else -> {
                    confirmGoShopping()
                }
            }
        }

        //-----------------------------------------------------------------------------------------------------------------------------------------

        //CREATES SWIPE MENU FOR LIST
        val creator = SwipeMenuCreator { menu ->
            // create "delete" item
            val deleteItem = SwipeMenuItem(
                applicationContext
            )
            // set item background
            deleteItem.background = ColorDrawable(
                Color.rgb(
                    0xF9, 0x3F, 0x25
                )
            )
            // set item width
            deleteItem.width = 200
            // set a icon
            deleteItem.setIcon(R.drawable.ic_delete_black_24dp)
            // add to menu
            menu.addMenuItem(deleteItem)
        }

        mainListView.setMenuCreator(creator)

        mainListView.setOnMenuItemClickListener { position, _, index ->
            when (index) {
                0 -> {
                    val removeItemToast = Toast.makeText( applicationContext, "Removed Item: " + item[position].getProduct(), Toast.LENGTH_SHORT
                    )
                    item.removeAt(position)
                    itemAdapter.notifyDataSetChanged()
                    removeItemToast.show()
                }
            }
            // false : close the menu; true : not close the menu
            false
        }
    }

    //LOGIC FOR GO SHOPPING
    private fun goShopping() {
        deleteItemsInDatabase()
        getSortedItemByPriority()
        for (x in 0 until item.size) {
            val budgetDoubleShopping: Double = (totalBudgetDouble() - item[x].getPrice().toDouble())
            val budgetSetter: String = ("$" + String.format(Locale.US, "%.2f", budgetDoubleShopping))

            if (item[x].getPriority() == 1) {
                if (budgetDoubleShopping < 0) {
                    mainListView.getChildAt(x).setBackgroundColor(Color.parseColor("#ff0f0f"))
                } else {
                    totalBudgetTextView.text = budgetSetter
                    mainListView.getChildAt(x).setBackgroundColor(Color.parseColor("#06d117"))
                    addItemsToDatabase(item[x])
                }
            } else if (item[x].getPriority() == 2) {
                if (budgetDoubleShopping < 0) {
                    mainListView.getChildAt(x).setBackgroundColor(Color.parseColor("#ff0f0f"))
                } else {
                    totalBudgetTextView.text = budgetSetter
                    mainListView.getChildAt(x).setBackgroundColor(Color.parseColor("#06d117"))
                    addItemsToDatabase(item[x])
                }
            } else if (item[x].getPriority() == 3) {
                if (budgetDoubleShopping < 0) {
                    mainListView.getChildAt(x).setBackgroundColor(Color.parseColor("#ff0f0f"))
                } else {
                    totalBudgetTextView.text = budgetSetter
                    mainListView.getChildAt(x).setBackgroundColor(Color.parseColor("#06d117"))
                    addItemsToDatabase(item[x])
                }
            } else {
                mainListView.getChildAt(x).setBackgroundColor(Color.parseColor("#ff0f0f"))
            }

        }
    }

    //CREATES DIALOG FOR USER TO CONFIRM THEY WANT TO GO SHOPPING
    private fun confirmGoShopping() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage(R.string.dialog_go_shopping)
            .setPositiveButton(R.string.dialog_yes) { _, _ -> goShopping()}
            .setNegativeButton(R.string.dialog_no) {_, _ ->  }
        val alert = dialogBuilder.create()
        alert.show()
    }

    //ADDS ITEM TO THE MAIN LIST VIEW
    private fun addItemToList() {
        val newItem = Item(getProductName(),priorityToInt(),quantityToInt(),getProductPrice())
        item.add(newItem)
    }

    //CHECKS IF ITEM ALREADY EXISTS IN THE LIST
    private fun checkForDuplicates() {
        var itemInList = false

        loop@
        for (i in 0 until item.size) {
            for (j in 0 until item.size) {
                if (item[i].getProduct().equals(productTextBox.text.toString(), true)) {
                    itemInList = true
                    break@loop
                }
            }
        }
        if (itemInList) {
            toastMessage("Item Is Already In Your Cart.")
        }
        else {
            addItemToList()
        }
    }

    //PRODUCT NAME FROM PRODUCT TEXT BOX AND CAPITALIZE FIRST LETTER
    private fun getProductName(): String {
        return productTextBox.text.toString().substring(0,1).toUpperCase() + productTextBox.text.toString().substring(1)
    }

    //PRODUCT PRICE FROM PRICE TEXT BOX
    private fun priceToDouble(): Double {
        return itemPriceTextBox.text.toString().toDouble()
    }

    //QUANTITY FROM COMBOBOX TO INTEGER
    private fun quantityToInt(): Int {
        return quantityComboBox.selectedItem.toString().toInt()
    }

    //PRIORITY FROM COMBOBOX TO INTEGER
    private fun priorityToInt(): Int {
        return priorityComboBox.selectedItem.toString().toInt()
    }

    //MULTIPLY ITEM PRICE AND ITEM QUANTITY AND CONVERT TO STRING
    private fun getProductPrice(): String {
        return String.format(Locale.US, "%.2f", (quantityToInt() * priceToDouble()))
    }

    //CONVERT BUDGET FROM STRING TO A DOUBLE
    private fun budgetToDouble(): Double {
        return budgetTextBox.text.toString().toDouble()
    }

    //REMOVES $ FROM BUDGET
    private fun budgetToString(): String {
        return totalBudgetTextView.text.toString().replace("$","")
    }

    //CONVERTS BUDGET FROM STRING TO DOUBLE
    private fun totalBudgetDouble(): Double {
        return budgetToString().toDouble()
    }

    //SORTS ITEM ARRAY BY PRIORITY
    private fun getSortedItemByPriority(): ArrayList<Item> {
        item.sortBy { it.getPriority() }
        return item
    }

    //TOAST MESSAGE
    private fun toastMessage(message: String) {
        return Toast.makeText(applicationContext,message,Toast.LENGTH_SHORT).show()
    }

    //ADDS ITEM TO DATABASE
    private fun addItemsToDatabase(newEntry:Item) {
        mDatabaseHelper.addItem(newEntry)
    }

    //CURSOR TO QUERY ITEMS IN DATABASE AND READ OUT IN TOAST TO USER
    private fun populateItemsInDatabase() {
        val data: Cursor = mDatabaseHelper.loadItems()
        val databaseItems: ArrayList<String> = ArrayList()
        while (data.moveToNext()) {
            databaseItems.add(data.getString(0))
            if (databaseItems.isNotEmpty()) {
                toastMessage("Items Bought Last Time:\n$databaseItems")
            } else {
                toastMessage("You Didn't Buy Any Items Last Time")
            }
        }
    }

    //DELETES ALL ITEMS WITHIN THE DATABASE TABLE
    private fun deleteItemsInDatabase() {
        mDatabaseHelper.deleteItems()
    }
}
