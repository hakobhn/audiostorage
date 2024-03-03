import React, { useEffect, useState } from 'react'
import { Navigate } from 'react-router-dom'
import { Container } from 'semantic-ui-react'
import { useAuth } from '../context/AuthContext'
import AdminTab from './AdminTab'
import { audioApi } from '../misc/AudioApi'
import { handleLogError } from '../misc/Helpers'

function AdminPage() {
  const Auth = useAuth()
  const user = Auth.getUser()

  const [users, setUsers] = useState([])
  const [files, setFiles] = useState([])
  const [audioName, setAudioName] = useState('')
  const [audioTextSearch, setAudioTextSearch] = useState('')
  const [userUsernameSearch, setUserUsernameSearch] = useState('')
  const [isAdmin, setIsAdmin] = useState(true)
  const [isUsersLoading, setIsUsersLoading] = useState(false)
  const [isFilesLoading, setIsFilesLoading] = useState(false)

  useEffect(() => {
    setIsAdmin(user.data.rol[0] === 'ADMIN')
    handleGetUsers()
    handleGetOrders()
  }, [])

  const handleInputChange = (e, { name, value }) => {
    if (name === 'userUsernameSearch') {
      setUserUsernameSearch(value)
    } else if (name === 'audioName') {
      setOrderDescription(value)
    } else if (name === 'audioTextSearch') {
      setOrderTextSearch(value)
    }
  }

  const handleGetUsers = async () => {
    setIsUsersLoading(true)
    try {
      const response = await audioApi.getUsers(user)
      setUsers(response.data)
    } catch (error) {
      handleLogError(error)
    } finally {
      setIsUsersLoading(false)
    }
  }

  const handleDeleteUser = async (username) => {
    try {
      await audioApi.deleteUser(user, username)
      handleGetUsers()
    } catch (error) {
      handleLogError(error)
    }
  }

  const handleSearchUser = async () => {
    const username = userUsernameSearch
    try {
      const response = await audioApi.getUsers(user, username)
      const data = response.data
      const users = data instanceof Array ? data : [data]
      setUsers(users)
    } catch (error) {
      handleLogError(error)
      setUsers([])
    }
  }

  const handleGetOrders = async () => {
    setIsOrdersLoading(true)
    try {
      const response = await audioApi.getFiles(user)
      setOrders(response.data)
    } catch (error) {
      handleLogError(error)
    } finally {
      setIsOrdersLoading(false)
    }
  }

  const handleDeleteOrder = async (isbn) => {
    try {
      await orderApi.deleteFile(user, isbn)
      handleGetFiles()
    } catch (error) {
      handleLogError(error)
    }
  }

  const handleCreateAudio = async () => {
    let description = audioName.trim()
    if (!description) {
      return
    }

    const order = { description }
    try {
      await audioApi.createFile(user, audio)
      handleGetFiles()
      setAudioName('')
    } catch (error) {
      handleLogError(error)
    }
  }

  const handleSearchFile = async () => {
    const text = audioTextSearch
    try {
      const response = await audioApi.getFiles(user, text)
      setOrders(response.data)
    } catch (error) {
      handleLogError(error)
      setOrders([])
    }
  }

  if (!isAdmin) {
    return <Navigate to='/' />
  }

  return (
    <Container>
      <AdminTab
        isUsersLoading={isUsersLoading}
        users={users}
        userUsernameSearch={userUsernameSearch}
        handleDeleteUser={handleDeleteUser}
        handleSearchUser={handleSearchUser}
        isAudiosLoading={isAudiosLoading}
        audios={audios}
        audioName={audioName}
        audioTextSearch={audioTextSearch}
        handleCreateFile={handleCreateFile}
        handleDeleteFile={handleDeleteFile}
        handleSearchAudio={handleSearchAudio}
        handleInputChange={handleInputChange}
      />
    </Container>
  )
}

export default AdminPage