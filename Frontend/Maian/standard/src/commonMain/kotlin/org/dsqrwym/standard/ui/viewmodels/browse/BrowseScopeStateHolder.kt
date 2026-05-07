package org.dsqrwym.standard.ui.viewmodels.browse

/**
 * 浏览范围状态管理器
 * 负责管理当前选中的批发商状态
 * 提供统一的状态管理接口
 */

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.dsqrwym.standard.domain.browse.RetailWholesaler

/**
 * 浏览范围状态
 * 封装当前浏览范围的相关信息
 * 
 * @param wholesalerId 批发商ID，null表示全局浏览
 * @param wholesalerName 批发商名称，用于显示
 * @param wholesaler 批发商对象，包含完整信息
 */
data class BrowseScopeState(
    val wholesalerId: String? = null,
    val wholesalerName: String? = null,
    val wholesaler: RetailWholesaler? = null,
)

/**
 * 浏览范围状态管理器
 * 使用 Compose 状态管理，提供状态更新接口
 */
class BrowseScopeStateHolder {
    /** 当前浏览范围状态，只能通过内部方法修改 */
    var state by mutableStateOf(BrowseScopeState())
        private set

    /**
     * 选中批发商
     * 更新状态为指定的批发商浏览范围
     * 
     * @param wholesaler 要选中的批发商
     */
    fun selectWholesaler(wholesaler: RetailWholesaler) {
        state = BrowseScopeState(
            wholesalerId = wholesaler.id,
            wholesalerName = wholesaler.displayName,
            wholesaler = wholesaler,
        )
    }

    /**
     * 清除批发商选择
     * 重置状态为全局浏览范围
     */
    fun clearWholesaler() {
        state = BrowseScopeState()
    }
}
