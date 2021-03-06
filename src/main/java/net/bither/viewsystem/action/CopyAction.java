/**
 * Copyright 2012 multibit.org
 *
 * Licensed under the MIT license (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://opensource.org/licenses/mit-license.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.bither.viewsystem.action;

import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.dialogs.MessageDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * This {@link Action} represents the swing copy receive address action
 */
public class CopyAction extends AbstractAction {
    public interface ICopy {
        public String getCopyString();
    }

    private ICopy copy;


    public CopyAction(ICopy copy) {

        this.copy = copy;
    }

    /**
     * Copy receive address to clipboard
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        // copy receive address to clipboard
        TextTransfer textTransfer = new TextTransfer();
        //getReceiveAddress
        textTransfer.setClipboardContents(copy.getCopyString());
        new MessageDialog(LocaliserUtils.getString("copy.address.success")).showMsg();


    }
}