package jjackjjack.sopt.com.jjackjjack.activities.donateparicipation

import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.webkit.URLUtil
import com.bumptech.glide.Glide
import jjackjjack.sopt.com.jjackjjack.R
import jjackjjack.sopt.com.jjackjjack.db.SharedPreferenceController
import jjackjjack.sopt.com.jjackjjack.model.DonateUsePlan
import jjackjjack.sopt.com.jjackjjack.list.DonateUsePlanRecyclerViewAdapter
import jjackjjack.sopt.com.jjackjjack.network.ApplicationController
import jjackjjack.sopt.com.jjackjjack.network.NetworkService
import jjackjjack.sopt.com.jjackjjack.network.data.DonatedDetailedData
import jjackjjack.sopt.com.jjackjjack.network.response.get.GetDonateParticipationDetailResponse
import jjackjjack.sopt.com.jjackjjack.utillity.ColorToast
import jjackjjack.sopt.com.jjackjjack.utillity.Secret
import kotlinx.android.synthetic.main.activity_donate_record_status.*
import kotlinx.android.synthetic.main.donate_status.*
import kotlinx.android.synthetic.main.fragment_berryuse_review.*
import kotlinx.android.synthetic.main.fragment_participation_status.*
import kotlinx.android.synthetic.main.fragment_use_berry.*
import kotlinx.android.synthetic.main.header_img.*
import kotlinx.android.synthetic.main.li_state.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class DonateParticipationStateActivity : AppCompatActivity() {
    val networkService: NetworkService by lazy {
        ApplicationController.instance.networkService
    }

    val dataList: ArrayList<DonateUsePlan> by lazy {
        ArrayList<DonateUsePlan>()
    }
    val dataList_DonateRecordStatus: ArrayList<DonateUsePlan> by lazy {
        ArrayList<DonateUsePlan>()
    }

    lateinit var donateUsePlanRecyclerViewAdapter: DonateUsePlanRecyclerViewAdapter

    val dec = DecimalFormat("#,000")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donate_record_status)
        initialUI()
    }

    private fun initialUI() {
        btn_toolbar_back.setOnClickListener {
            finish()
        }
        var list: ArrayList<DonateUsePlan> = ArrayList()
        initialUI_status()
    }

    private fun getDonateParticipateDetailResponse() {

        val programId: String = intent.getStringExtra("programId_status")

        val token = SharedPreferenceController.getAuthorization(this)
        val getDonateParticipationDetailResponse =
            networkService.getDonateParticipationDetailResponse("application/json", token, programId)

        getDonateParticipationDetailResponse.enqueue(object : Callback<GetDonateParticipationDetailResponse> {
            override fun onFailure(call: Call<GetDonateParticipationDetailResponse>, t: Throwable) {
                Log.e("DB error", t.toString())
                ColorToast(this@DonateParticipationStateActivity, "잠시 후 다시 접속해주세요")
            }

            override fun onResponse(
                call: Call<GetDonateParticipationDetailResponse>,
                response: Response<GetDonateParticipationDetailResponse>
            ) {
                if (response.isSuccessful) {
                    if (response.body()!!.status == Secret.NETWORK_SUCCESS) {
                        val receiveData: ArrayList<DonatedDetailedData> = response.body()!!.data
                        if (receiveData.size > 0) {
                            for (i in 0 until receiveData.size) {
                                if (URLUtil.isValidUrl(receiveData[i].thumbnail)) {
                                    Glide.with(this@DonateParticipationStateActivity).load(receiveData[i].thumbnail)
                                        .into(header_image_view)
                                }
                                donate_detailed_title.text = receiveData[i].title
                                donate_detailed_association.text = receiveData[i].centerName
                                participation_status_date_ing.text = (receiveData[i].start).split("T")[0] + " ~"
                                participation_status_date_end.text = "~ " + (receiveData[i].finish).split("T")[0]
                                participation_status_date_finish.text = (receiveData[i].deliver).split("T")[0] + " (예정)"
                                li_state_d_day.text = "- " + converteDday(receiveData[i].finish)
                                li_state_percent.text = receiveData[i].percentage.toString()
                                li_state_berry_num.text = dec.format(receiveData[i].totalBerry).toString()
                                li_state_total_num.text = dec.format(receiveData[i].maxBerry).toString()
                                li_state_progress.progress = receiveData[i].percentage

                                if (receiveData[i].state == 0) {
                                    circle_status_ing.setImageResource(R.drawable.shape_donate_status_circle_ing)
                                    donate_record_status_review.visibility = View.GONE
                                    li_state_progress.progressDrawable.setColorFilter(
                                        Color.parseColor("#da4830"),
                                        PorterDuff.Mode.SRC_IN
                                    )
                                    li_state_percent_mark.setTextColor(Color.parseColor("#da4830"))
                                    li_state_percent.setTextColor(Color.parseColor("#da4830"))
                                    tv_participation_ing.setTextColor(Color.parseColor("#da4830"))
                                    participation_status_ing.setTextColor(Color.parseColor("#333333"))
                                } else if (receiveData[i].state == 1) {
                                    circle_status_end.setImageResource(R.drawable.shape_donate_status_circle_end)
                                    li_state_d_mark.visibility = View.INVISIBLE
                                    li_state_d_day.visibility = View.INVISIBLE
                                    donate_record_status_review.visibility = View.GONE
                                    li_state_progress.progressDrawable.setColorFilter(
                                        Color.parseColor("#86b854"),
                                        PorterDuff.Mode.SRC_IN
                                    )
                                    li_state_percent_mark.setTextColor(Color.parseColor("#86b854"))
                                    li_state_percent.setTextColor(Color.parseColor("#86b854"))
                                    tv_participation_end.setTextColor(Color.parseColor("#86b854"))
                                    participation_status_end.setTextColor(Color.parseColor("#333333"))
                                } else if (receiveData[i].state == 2) {
                                    circle_status_finish.setImageResource(R.drawable.shape_donate_status_circle_finish)
                                    li_state_d_mark.visibility = View.INVISIBLE
                                    li_state_d_day.visibility = View.INVISIBLE
                                    li_state_total_num.visibility = View.INVISIBLE
                                    li_state_berry_num.text = dec.format(receiveData[i].totalBerry).toString()
                                    li_state_total_num_berry.visibility = View.INVISIBLE
                                    li_state_progress.progressDrawable.setColorFilter(
                                        Color.parseColor("#464fb2"),
                                        PorterDuff.Mode.SRC_IN
                                    )
                                    li_state_percent_mark.setTextColor(Color.parseColor("#464fb2"))
                                    li_state_percent.setTextColor(Color.parseColor("#464fb2"))
                                    tv_participation_finish.setTextColor(Color.parseColor("#464fb2"))
                                    participation_status_finish.setTextColor(Color.parseColor("#333333"))

                                    if (receiveData[0].review.size > 0) { //수정해야함
                                        donate_record_status_review.visibility = View.VISIBLE
                                        participation_status_finish.visibility = View.INVISIBLE

                                        use_story_title.text = receiveData[i].review[i].story!![i].subTitle
                                        use_story_content1.text = receiveData[i].review[i].story!![i].content!![0]
                                        use_story_content2.text = receiveData[i].review[i].story!![i].content!![1]

                                        if (URLUtil.isValidUrl(receiveData[i].review[i].story!![i].img)) {
                                            Glide.with(this@DonateParticipationStateActivity)
                                                .load(receiveData[i].review[i].story!![i].img)
                                                .into(use_story_img)
                                        }
                                    }


                                    var sum = 0

                                    for (z in 0 until receiveData[i].plan!!.size) {
                                        dataList_DonateRecordStatus.add(
                                            DonateUsePlan(
                                                (z + 1).toString(),
                                                receiveData[i].plan!![z].purpose,
                                                dec.format(receiveData[i].plan!![z].price).toString()
                                            )
                                        )
                                        sum = sum + receiveData[i].plan!![z].price.toString().toInt()
                                        updateDonateRecordStatus(dataList_DonateRecordStatus)
                                    }
                                    use_berry_total.text = dec.format(sum).toString()
                                }
                            }
                        }
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        getDonateParticipateDetailResponse()
    }

    private fun updateDonateRecordStatus(list: ArrayList<DonateUsePlan>) {
        dataList.clear()
        dataList.addAll(list)
        donateUsePlanRecyclerViewAdapter.notifyDataSetChanged()
    }

    private fun initialUI_status() {
        donateUsePlanRecyclerViewAdapter =
            DonateUsePlanRecyclerViewAdapter(this, dataList)
        rv_donate_use_container.adapter = donateUsePlanRecyclerViewAdapter
        rv_donate_use_container.layoutManager = LinearLayoutManager(this)
    }

    private fun converteDday(finish: String): String {

        var dday: Int = 0
        var Dday: String = ""

        if (finish != null) {

            val today = Calendar.getInstance()
            val finishdateFormat = SimpleDateFormat("yyyy-MM-dd").parse(finish.split("T")[0])
            val instance: Calendar = Calendar.getInstance()
            instance.setTime(finishdateFormat)

            val cnt_today: Long = today.timeInMillis / 86400000
            val cnt_instance: Long = instance.timeInMillis / 86400000

            val sub: Long = cnt_today - cnt_instance

            dday = Math.abs(sub.toInt() + 1)
            Dday = "$dday"

        }
        return Dday
    }
}