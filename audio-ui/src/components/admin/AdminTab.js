import React from 'react'
import { Tab } from 'semantic-ui-react'
import UserTable from './UserTable'
import AudioTable from './AudioTable'

function AdminTab(props) {
  const { handleInputChange } = props
  const { isUsersLoading, users, userUsernameSearch, handleDeleteUser, handleSearchUser } = props
  const { isAudiosLoading, audios, audioName, audioTextSearch, handleCreateAudio, handleDeleteAudio, handleSearchAudio } = props

  const panes = [
    {
      menuItem: { key: 'users', icon: 'users', content: 'Users' },
      render: () => (
        <Tab.Pane loading={isUsersLoading}>
          <UserTable
            users={users}
            userUsernameSearch={userUsernameSearch}
            handleInputChange={handleInputChange}
            handleDeleteUser={handleDeleteUser}
            handleSearchUser={handleSearchUser}
          />
        </Tab.Pane>
      )
    },
    {
      menuItem: { key: 'audios', icon: 'file', content: 'Audios' },
      render: () => (
        <Tab.Pane loading={isOAudiosLoading}>
          <AudioTable
            audios={audios}
            audioName={audioName}
            orderTextSearch={orderTextSearch}
            handleCreateAudio={handleCreateAudio}
            handleDeleteAudio={handleDeleteAudio}
          />
        </Tab.Pane>
      )
    }
  ]

  return (
    <Tab menu={{ attached: 'top' }} panes={panes} />
  )
}

export default AdminTab